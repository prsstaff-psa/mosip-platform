node {
/* this Jenkins file needs fixes for single repo kernel */
 try{
 def server = Artifactory.server 'ART'
 def rtMaven = Artifactory.newMavenBuild()
 def buildInfo
 def branch = env.NEXT_BRANCH_NAME
 def dockerImagesTagVersion = 'NaV' //This will ensure that docker images tag should have 1-1 relation ship with project version
 projectToBuild = 'kernel'
 def registryUrl = 'http://docker-registry.mosip.io:5000'
 def registryName = 'docker-registry.mosip.io:5000'
 def registryCredentials = '305c7f35-2eb5-44b4-b574-a0855ef70c26'
 targetDeploymentServer = 'kernel-dev-server'
 updatedServices = []
 updatedDockerImages = []
 containersToRun = ''
 containersNames = ''
 k8DevContext = 'dev-k8-cluster'


 stage('------- Checkout --------') {
  dir(branch) {
   checkout([$class: 'GitSCM',
    branches: [
     [name: branch]
    ],
    userRemoteConfigs: [
     [url: 'https://github.com/prsstaff-psa/mosip-platform', credentialsId: '5c295f4b-0a45-4a46-903f-bb92da7d74c1']
    ],
    extensions: [
     /*
     Using PathRestriction Class and adding Kernel in includedRegions so that the build will be triggered
     only if there is any change in Kernel Module
     */
     [$class: 'PathRestriction', excludedRegions: '', includedRegions: projectToBuild + '/.*'],
     /*
     Checkout only Kernel Module through sparse checkout class
     */
     [$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [
      [$class: 'SparseCheckoutPath', path: '/' + projectToBuild + '/']
     ]]

    ],
   ])
  }

 }

 stage('--------- Artifactory configuration ----------------') {
  /*
  JFrog artifactory configuration
  */
  rtMaven.tool = 'M2_HOME' // Tool name from Jenkins configuration
  rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
  rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server
  buildInfo = Artifactory.newBuildInfo()
  buildInfo.env.capture = true
 }

 stage('---------- mvn-clean-install and push to artifactory  ---------------') {
  rtMaven.run pom: branch + '/' + projectToBuild + '/pom.xml', goals: 'clean install', buildInfo: buildInfo
 }

stage('---------- SonarQube Analysis --------------') {
  def mvnHome = tool name: 'M2_HOME', type: 'maven'
  withSonarQubeEnv('sonar') {
   sh "${mvnHome}/bin/mvn -f '$branch/$projectToBuild/' sonar:sonar"
  }
 }

 stage('----------- Publish build info -------------') {
  /*
  Publishing build info to Artifcatory (JFrog)
  */
  server.publishBuildInfo buildInfo
 }

 stage('------ Docker Images : Push & Cleanup -------') {
  /*
   Building Docker images of the services which have to be run inside a Docker Container and pushing the
   images to Docker Registry.
   This stage will build either all the Services (modules) present or only modules that have been changed
   since last build according to the build parameter provided.
  */
  dir(branch) {
	/*
	finding parent pom version to create Docker tags
	*/
   pomFile = readMavenPom file: projectToBuild+'/pom.xml'
   dockerImagesTagVersion = pomFile.parent.version
   echo dockerImagesTagVersion

   /*
     Checking if there is any Dockerfile present to build, if not, returning from this stage
   */
   sh "find . -name 'Dockerfile'> testdirectoryDockerFiles"
   testlocationOfDockerFiles = readFile("testdirectoryDockerFiles").split("\\r?\\n");
   sh "rm -f testdirectoryDockerFiles"
   if (testlocationOfDockerFiles[0].equals("")) {
      echo "tested:::: no docker files present, returning"
      return;
     }
   /*
   Finding location of all the Dockerfiles present in module
   */
   sh "dirname `find . -name 'Dockerfile'`> directoryDockerFiles"
   locationOfDockerFiles = readFile("directoryDockerFiles").split("\\r?\\n");
   sh "rm -f directoryDockerFiles"
   /*
   If build paramerter is passed as Build All (All submodules have to be built)
   */
   if ("${env.BUIILD_OPTION}" == "Build All") {
    echo "BUILD_OPTION is set to Build All."
    echo "Building all submodules."
    for (int i = 0; i < locationOfDockerFiles.size(); i++) {
     if (locationOfDockerFiles[i].equals("")) {
      break;
     }
     def currentModule = locationOfDockerFiles[i]
     /*
     Getting name of the module to be built
     */
     sh "basename $currentModule > moduleName"
     moduleName = readFile('moduleName').trim()
     sh "rm -f moduleName"
     /*
     Getting path of the module to be built
     */
     modulePath = locationOfDockerFiles[i].substring(2);
     updatedServices.add(moduleName)
     updatedDockerImages.add(registryName + '/' + moduleName + ":$dockerImagesTagVersion.$BUILD_NUMBER")
     /*
     Updating containersToRun List
     */
     containersToRun = containersToRun + " " + registryName + '/' + moduleName
     /*
     Updating containersName List
     */
     containersNames = containersNames + " " + moduleName
     echo moduleName + " will be built"
     /*
     Pushing the module image with tag as version.buildnumber and tag as latest to docker registry
     */
     docker.withRegistry(registryUrl, registryCredentials) {
      def buildName = moduleName + ":$dockerImagesTagVersion.$BUILD_NUMBER"
      newApp = docker.build(buildName, '-f ' + currentModule + "/Dockerfile" + ' ' + modulePath)
      newApp.push()
      newApp.push 'latest'
     }
     /*
     Removing local images
     */
     sh "docker rmi $moduleName:$dockerImagesTagVersion.$BUILD_NUMBER"
     sh "docker rmi $registryName/$moduleName:$dockerImagesTagVersion.$BUILD_NUMBER"
     sh "docker rmi $registryName/$moduleName"
    }
   }
   else {
    /*
     If build paramerter is passed as Build Changes (Only changed submodules have to be built)
    */
    echo "BUILD_OPTION is set to Build Changed."
    echo "Building only changed submodules."
    changedModulesLocation = getChangedModulesPaths();
    for (int i = 0; i < changedModulesLocation.size(); i++) {
     def currentModule = changedModulesLocation[i]
     /*
     Getting name of the module to be built
     */
     sh "basename $currentModule > moduleName"
     moduleName = readFile('moduleName').trim()
     sh "rm -f moduleName"
     /*
     Getting path of the module to be built
     */
     modulePath = changedModulesLocation[i].substring(2);
     updatedServices.add(moduleName)
     updatedDockerImages.add(registryName + '/' + moduleName + ":$dockerImagesTagVersion.$BUILD_NUMBER")
     /*
     Updating containersToRun List
     */
     containersToRun = containersToRun + " " + registryName + '/' + moduleName
     /*
     Updating containersName List
     */
     containersNames = containersNames + " " + moduleName
     echo moduleName + " will be built"
     /*
     Pushing the module image with tag as version.buildnumber and tag as latest to docker registry
     */
     docker.withRegistry(registryUrl, registryCredentials) {
      def buildName = moduleName + ":$dockerImagesTagVersion.$BUILD_NUMBER"
      newApp = docker.build(buildName, '-f ' + currentModule + "/Dockerfile" + ' ' + modulePath)
      newApp.push()
      newApp.push 'latest'
     }
     /*
     Removing local images
     */
     sh "docker rmi $moduleName:$dockerImagesTagVersion.$BUILD_NUMBER"
     sh "docker rmi $registryName/$moduleName:$dockerImagesTagVersion.$BUILD_NUMBER"
     sh "docker rmi $registryName/$moduleName"
    }
   }

    /*
     Removing all untagged images
     */
    sh "docker rmi \$(docker images | awk '/<none>/ {print \$3}') || true"

  }

 }

/*
 // uncomment out this stage and comment out next stage 'Kubernetes Deploy : DEV-INT' if you want to run Docker containers without Kubernetes cluster
  stage('----deploy in dev----')
 {

     //All the Deployment steps that have to be performed in deployment server are written in this command string

	  command = """
 		INDEX=0
		containersToRunShell=(${containersToRun})
		containersNamesShell=(${containersNames})

 		for i in "\${containersToRunShell[@]}"
			do
			  echo "Deploying Service - " \$i
			  docker pull \$i
			  portExposed="\$(docker inspect --format='{{range \$p, \$conf := .Config.ExposedPorts}} {{index (split \$p "/") 0}} {{end}}' \$i)"
			  portExposedFormatted="\$(echo \$portExposed| xargs)"
			  echo Container will expose services on \$portExposedFormatted
			  volumeMounted="\$(docker inspect --format='{{range \$p, \$conf := .Config.Volumes}} {{ \$p }} {{end}}' \$i)"
			  portExposedFormatted="\$(echo \$portExposed| xargs)"
			 if [ "\$(docker ps -a | grep \${containersNamesShell[\$INDEX]})" ]; then
			  docker rename \${containersNamesShell[\$INDEX]} \${containersNamesShell[\$INDEX]}-backup && docker stop \${containersNamesShell[\$INDEX]}-backup
			 fi
			  if [ "\$volumeMounted" = "" ]; then
					echo "No volume to mount"
					 docker run -d -e active_profile_env=dev -p \$portExposedFormatted:\$portExposedFormatted --name "\${containersNamesShell[\$INDEX]}" \$i
					  if [ \$? -eq 0 ]; then
						echo Container Started - \${containersNamesShell[\$INDEX]}
						if [ "\$(docker ps -a | grep \${containersNamesShell[\$INDEX]}-backup)" ]; then
							echo Removing Backed up container - \${containersNamesShell[\$INDEX]}-backup
							docker rm \${containersNamesShell[\$INDEX]}-backup
						fi
					  else
						echo Error - Cannot start new container, rollbacking to previous container
						docker start \${containersNamesShell[\$INDEX]}-backup && docker rename \${containersNamesShell[\$INDEX]}-backup \${containersNamesShell[\$INDEX]}
					  fi
			  else
					echo "Volume to me mounted : \$volumeMounted"
					currentDirectory=\$(echo \$(pwd))
					volumeMountedFormatted="\$(echo \$volumeMounted| xargs)"
					 docker run -d -v \$currentDirectory\$volumeMountedFormatted:\$volumeMountedFormatted -e active_profile_env=dev -p \$portExposedFormatted:\$portExposedFormatted --name "\${containersNamesShell[\$INDEX]}" \$i
					  if [ \$? -eq 0 ]; then
						echo Container Started - \${containersNamesShell[\$INDEX]}
						if [ "\$(docker ps -a | grep \${containersNamesShell[\$INDEX]}-backup)" ]; then
							echo Removing Backed up container - \${containersNamesShell[\$INDEX]}-backup
							docker rm \${containersNamesShell[\$INDEX]}-backup
						fi
					  else
						echo Error - Cannot start new container, rollbacking to previous container
						docker start \${containersNamesShell[\$INDEX]}-backup && docker rename \${containersNamesShell[\$INDEX]}-backup \${containersNamesShell[\$INDEX]}
					  fi
			  fi;
			  let INDEX=\${INDEX}+1
			done

			if [ "\$(docker images | grep  '<none>')" ]; then
				echo Removing all untagged images
				docker rmi \$(docker images | awk '/<none>/ {print \$3}')
			fi
            echo Deployment process completed
    """

    //Using sshPublisher class to publish this command over to the deployement server

        sshPublisher(publishers: [sshPublisherDesc(configName: targetDeploymentServer, transfers: [sshTransfer(execCommand: "$command")])])
    }
    */

    stage('Kubernetes Deploy : DEV'){
      //this stage will rollout the changes on Kubernetes Cluster
      sh "kubectl config use-context $k8DevContext"
      for(int i = 0; i<updatedDockerImages.size(); i++){
        def updatedDockerImage = updatedDockerImages[i]
        def updatedService = updatedServices[i]
        echo "Updating [ Service:$updatedService, Image:$updatedDockerImage ]"
        sh "kubectl set image deployment/$updatedService $updatedService=$updatedDockerImage"
      }
      echo "Getting list of all services"
      sh "kubectl get pods"
    }
 }

   // Catching the exception for triggering email
  catch (exception) {
        print exception
		// If the job was aborted by any user
        if (exception instanceof InterruptedException) {
           currentBuild.result = "ABORTED"
        }
		// If the job failed due to some error
        else{
            currentBuild.result = "FAILURE"
        }

        throw exception //rethrow exception to prevent the build from proceeding


        }
	finally{
		if(currentBuild.result == "FAILURE"){
		// sending email to kernel recipients
		recipients = "$env.KERNEL_RECIPIENT_LIST"
		emailext (
					subject: "MOSIP Jenkins Job $JOB_NAME with build no $BUILD_NUMBER failed'",
					body: """<p>Check console output at <a href="$BUILD_URL">'${JOB_NAME}'</a></p>""",
					to: "$recipients",
					from: '"Jenkins" <info@mosip.io>'
				)
	}
	}
}

/*This function will return paths all the Submodules which have been changed and
 which will be deployed as a service*/
@NonCPS
def getChangedModulesPaths() {
 Set modifiedModulePaths = [] as TreeSet

 /*
 ChangeSets until last build
 */
 def changeLogSets = currentBuild.changeSets

 /*
 A treeSet to store paths of all the files that have been changed until last build
 */
 Set affectedPaths = [] as TreeSet
 for (int i = 0; i < changeLogSets.size(); i++) {
  def entries = changeLogSets[i].items
  for (int j = 0; j < entries.length; j++) {
   def entry = entries[j]
   echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
   affectedPaths.addAll(entry.affectedPaths);
   }
  }
  /*
  Filtering all the modules from affected path that do not belong to project to build
  */
  echo "affected paths are :" + affectedPaths
  affectedPaths.retainAll{it.startsWith(projectToBuild)}
  echo "affected paths after retaining only kernel modules are --------:" + affectedPaths
for (int i = 0; i < locationOfDockerFiles.size(); i++) {
        /*
        If there is no Dockerfile present in the Services
        */
        if(locationOfDockerFiles[i].equals("")){
        	break;
        }
	/*
        Checking if the paths affected belong to modules that have to be deployed
        */
		if(affectedPaths.any{it.startsWith(locationOfDockerFiles[i].substring(2))}){
			modifiedModulePaths.add(locationOfDockerFiles[i])
            /*
            Removing affected paths that belong to same module (Because we have already taken
            into consideration this module)
            */
			affectedPaths.removeAll{it.startsWith(locationOfDockerFiles[i].substring(2))}
		}



  }
	echo "paths of modules to be built ::::::::::::>" + modifiedModulePaths
 	return modifiedModulePaths
}
