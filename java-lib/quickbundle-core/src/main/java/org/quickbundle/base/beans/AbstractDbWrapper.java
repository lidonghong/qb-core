package org.quickbundle.base.beans;

import org.quickbundle.ICoreConstants;
import org.quickbundle.config.RmBaseConfig;

public abstract class AbstractDbWrapper {
	protected TableIdRuleVo ruleVo = null;
	public AbstractDbWrapper(TableIdRuleVo ruleVo) {
		this.ruleVo = ruleVo;
	}
    
    //根据 集群+table的前缀和maxId，获取下一个可用值或默认起始值
   protected long getMaxIdOrDefault(String maxId) {
    	long result = 0L;
		if(maxId != null && maxId.length() > 0) {
			if(maxId.length() > 19) {
				result = Long.parseLong(maxId.substring(0, 19)) + 1;
			} else {
				result = Long.parseLong(maxId) + 1;
			}
		} else {
			result = Long.parseLong(firstValue());
		}
    	return result;
    }
   
	protected String firstValue() {
		return RmBaseConfig.getSingleton().getShardingPrefix() + ruleVo.getTableCode() + "00000000001";
	}
	
	private String getShardingPrefix() {
		return RmBaseConfig.getSingleton().getShardingPrefix() + ruleVo.getTableCode();
	}
	
    //根据<table table_code="2003" table_name="RM_AFFIX" id_name="ID"/>和shardingPrefix，获得查询最大值的sql
	protected String getSqlSelectMax() {
		StringBuilder sql = new StringBuilder();
		sql.append("select max(");
		sql.append(ruleVo.getIdName());
		sql.append(") max_id from ");
		sql.append(ruleVo.getTableName());
		sql.append(" where ");
		sql.append(parseColumn(ruleVo.getIdName()));
		sql.append(" like '");
		sql.append(getShardingPrefix());
		sql.append("%'");
    	return sql.toString();
    }
    
    private String parseColumn(String idName) {
    	StringBuilder result = new StringBuilder();
    	String dpn = RmBaseConfig.getSingleton().getDatabaseProductName();
    	//db2需要char(columnName) like '10001000%'
    	if(dpn == null 
    			|| ICoreConstants.DatabaseProductType.DB2.getDatabaseProductName().equals(dpn)
    			|| dpn.startsWith(ICoreConstants.DatabaseProductType.DB2.getDatabaseProductName() + "/")) {
    		result.append("char(").append(idName).append(")");
    		return result.toString();
    	} else {
    		return idName;
    	}
    }
}
