-- create table section ------------------------------------------------------------
-- schema 		: master  - Master Reference schema
-- table 		: machine_spec  - Master machine_spec list
-- table alias  : mspec	
   
-- schemas section -----------------------------------------------------------------
 
-- create schema if master reference schema not exists
create schema if not exists master
;

-- table section --------------------------------------------------------------------

	create table master.machine_spec ( 
	
		id character varying(36) not null ,
		
		name  character varying (64) not null ,			
		brand character varying(32) not null ,			-- make
		model character varying(16) not null ,
		
		mtyp_code  character varying(36) not null ,     -- master.machine_type.code
		
		min_driver_ver character varying(16) not null ,
		descr character varying (256) ,
		
		lang_code  character varying(3) not null ,	-- master.language.code
	
		is_active 	boolean not null,
		cr_by 		character varying (256) not null,
		cr_dtimes 	timestamp  not null,
		upd_by  	character varying (256),
		upd_dtimes timestamp ,
		is_deleted 	boolean,
		del_dtimes	timestamp 
	)
;

-- keys section ---------------------------------------------------------------------------
alter table master.machine_spec add constraint pk_mspec_code primary key (id, lang_code)
 ;

-- indexes section ------------------------------------------------------------------------
-- create index idx_mspec_<col> on master.machine_spec (col)
-- ;

