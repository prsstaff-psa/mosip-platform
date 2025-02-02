-- create table section --------------------------------------------------------
-- schema 		: master   - Master Reference schema
-- table 		: appl_form_type - Master application form type list (new , update, correction etc application forms)
-- table alias  : applftyp	
 
-- schemas section ---------------------------------------------------------------

-- create schema if master reference schema not exists
create schema if not exists master
;
 
-- table section -------------------------------------------------------------------------------

	create table master.appl_form_type (
	
		code   character varying(36) not null ,
		name   character varying (64) not null ,
		descr  character varying (128) ,
		
		lang_code  character varying(3) not null , -- master.language.code
	
		is_active 	boolean not null,
		cr_by 		character varying (256) not null,
		cr_dtimes 	timestamp  not null,
		upd_by  	character varying (256),
		upd_dtimes  timestamp ,
		is_deleted 	boolean,
		del_dtimes	timestamp 

	)
;
		

-- keys section -------------------------------------------------------------------------------
alter table master.appl_form_type add constraint pk_applftyp_code primary key (code, lang_code)
 ;

-- indexes section -----------------------------------------------------------------------
-- create index idx_applftyp_<col> on master.appl_form_type (col)
-- ;

-- comments section -------------------------------------------------------------------------- 
comment on table master.appl_form_type is 'Master application_type table'
;

