/**
 * 
 */
package io.mosip.kernel.core.cbeffutil.entity;

import java.util.List;

import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;

/**
 * 
 * BIR class with Builder to create data
 * 
 * @author Ramadurai Pandian
 *
 */
public class BIR {

	private BIRVersion version;
	private BIRVersion cbeffversion;
	private BIRInfo birInfo;
	private BDBInfo bdbInfo;
	private byte[] bdb;
	private byte[] sb;
	private SBInfo sbInfo;
	private List<Object> element;
	private List<BIR> birList;

	public BIR(BIRBuilder birBuilder) {
		this.version = birBuilder.version;
		this.cbeffversion = birBuilder.cbeffversion;
		this.birInfo = birBuilder.birInfo;
		this.bdbInfo = birBuilder.bdbInfo;
		this.bdb = birBuilder.bdb;
		this.sb = birBuilder.sb;
		this.sbInfo = birBuilder.sbInfo;
		this.element = birBuilder.element;
	}

	/**
	 * @return the element
	 */
	public List<Object> getElement() {
		return element;
	}

	/**
	 * @return the version
	 */
	public BIRVersion getVersion() {
		return version;
	}

	/**
	 * @return the cbeffversion
	 */
	public BIRVersion getCbeffversion() {
		return cbeffversion;
	}

	/**
	 * @return the birInfo
	 */
	public BIRInfo getBirInfo() {
		return birInfo;
	}

	/**
	 * @return the bdbInfo
	 */
	public BDBInfo getBdbInfo() {
		return bdbInfo;
	}

	/**
	 * @return the bdb
	 */
	public byte[] getBdb() {
		return bdb;
	}

	/**
	 * @return the sb
	 */
	public byte[] getSb() {
		return sb;
	}

	/**
	 * @return the sbInfo
	 */
	public SBInfo getSbInfo() {
		return sbInfo;
	}

	/**
	 * @return the sbInfo
	 */
	public List<BIR> getBirList() {
		return birList;
	}
	
	/**
	 * @param birList the birList to set
	 */
	public void setBirList(List<BIR> birList) {
		this.birList = birList;
	}

	public static class BIRBuilder {
		private BIRVersion version;
		private BIRVersion cbeffversion;
		private BIRInfo birInfo;
		private BDBInfo bdbInfo;
		private byte[] bdb;
		private byte[] sb;
		private SBInfo sbInfo;
		private List<Object> element;

		public BIRBuilder withElement(List<Object> element) {
			this.element = element;
			return this;
		}

		public BIRBuilder withVersion(BIRVersion version) {
			this.version = version;
			return this;
		}

		public BIRBuilder withCbeffversion(BIRVersion cbeffversion) {
			this.cbeffversion = cbeffversion;
			return this;
		}

		public BIRBuilder withBirInfo(BIRInfo birInfo) {
			this.birInfo = birInfo;
			return this;
		}

		public BIRBuilder withBdbInfo(BDBInfo bdbInfo) {
			this.bdbInfo = bdbInfo;
			return this;
		}

		public BIRBuilder withBdb(byte[] bdb) {
			this.bdb = bdb;
			return this;
		}

		public BIRBuilder withSb(byte[] sb) {
			this.sb = sb;
			return this;
		}

		public BIRBuilder withSbInfo(SBInfo sbInfo) {
			this.sbInfo = sbInfo;
			return this;
		}

		public BIR build() {
			return new BIR(this);
		}

	}

	public BIRType toBIRType(BIR bir) {
		BIRType bIRType = new BIRType();
		if (bir.getCbeffversion() != null)
			bIRType.setVersion(bir.getCbeffversion().toVersion());
		bIRType.setBDB(getBdb());
		bIRType.setSB(getSb());
		if (bir.getBirInfo() != null)
			bIRType.setBIRInfo(bir.getBirInfo().toBIRInfo());
		if (bir.getBdbInfo() != null)
			bIRType.setBDBInfo(bir.getBdbInfo().toBDBInfo());
		if (bir.getSbInfo() != null)
			bIRType.setSBInfo(bir.getSbInfo().toSBInfoType());
		if (bir.getElement() != null)
			bIRType.setAny(getElement());
		return bIRType;
	}

}
