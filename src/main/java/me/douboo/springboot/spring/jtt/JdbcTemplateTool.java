package me.douboo.springboot.spring.jtt;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import me.douboo.springboot.spring.jtt.exception.NoColumnAnnotationFoundException;
import me.douboo.springboot.spring.jtt.exception.NoDefinedGetterException;
import me.douboo.springboot.spring.jtt.exception.NoIdAnnotationFoundException;
import me.douboo.springboot.spring.jtt.impl.BatchUpdateSetter;
import me.douboo.springboot.spring.jtt.model.SqlParamsPairs;
import me.douboo.springboot.spring.jtt.utils.IdUtils;
import me.douboo.springboot.spring.jtt.utils.ModelSqlUtils;

/**
 * Enhance JdbcTemplate
 * 
 * @author alexy
 *
 */
public class JdbcTemplateTool {

	private JdbcTemplate jdbcTemplate;

	// JdbcTemplateTool use proxy instead of directly use jdbcTemplate, cause it can
	// do some AOP function in proxy. That makes code more clear.
	private JdbcTemplateProxy _proxy;

	public JdbcTemplateTool() {
		super();
	}

	public JdbcTemplateTool(JdbcTemplate jdbcTemplate) {
		super();
		setJdbcTemplate(jdbcTemplate);
	}

	// return the singleton proxy
	private JdbcTemplateProxy getProxy() {
		if (_proxy == null) {
			_proxy = new JdbcTemplateProxy();
			_proxy.setJdbcTemplate(jdbcTemplate);
		}
		return _proxy;
	}

	// --------- select ------------//

	/**
	 * 获取对象列表 get a list of clazz
	 * 
	 * @param sql
	 * @param params
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> list(String sql, Object[] params, Class<T> clazz) {

		// call jdbcTemplate to query for result
		List<T> list = null;
		if (params == null || params.length == 0) {
			list = getProxy().query(sql, new BeanPropertyRowMapper(clazz));
		} else {
			list = getProxy().query(sql, params, new BeanPropertyRowMapper(clazz));
		}

		// return list
		return list;
	}

	/**
	 * 获取单个对象，不存在则返回null
	 * 
	 * @author luheng
	 * @param sql
	 * @param params
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T get(String sql, Object[] params, Class<T> clazz) {

		// call jdbcTemplate to query for result
		List<T> list = null;
		if (params == null || params.length == 0) {
			list = getProxy().query(sql, new BeanPropertyRowMapper(clazz));
		} else {
			list = getProxy().query(sql, params, new BeanPropertyRowMapper(clazz));
		}

		// return list
		return !CollectionUtils.isEmpty(list) ? list.get(0) : null;
	}

	/**
	 * 获取对象列表 get a list of clazz
	 * 
	 * @param sql
	 * @param clazz
	 * @return
	 */
	public <T> List<T> list(String sql, Class<T> clazz) {

		// call jdbcTemplate to query for result
		return this.list(sql, null, clazz);
	}

	/**
	 * 获取总行数 get count
	 * 
	 * @param sql
	 * @param params
	 * @param clazz
	 * @return
	 */
	public int count(String sql, Object[] params) {

		int rowCount = 0;
		try {
			Map<String, Object> resultMap = null;
			if (params == null || params.length == 0) {
				resultMap = getProxy().queryForMap(sql);
			} else {
				resultMap = getProxy().queryForMap(sql, params);
			}
			Iterator<Map.Entry<String, Object>> it = resultMap.entrySet().iterator();
			if (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				rowCount = ((Long) entry.getValue()).intValue();
			}
		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rowCount;
	}

	/**
	 * 获取总行数 get count
	 * 
	 * @param sql
	 * @param params
	 * @param clazz
	 * @return
	 */
	public int count(String sql) {
		return this.count(sql, null);
	}

	/**
	 * 获取一个对象 get object by id
	 * 
	 * @param sql
	 * @param params
	 * @param clazz
	 * @return
	 * @throws NoIdAnnotationFoundException
	 * @throws NoColumnAnnotationFoundException
	 * @throws NoDefinedGetterException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T get(Class clazz, Object id) {
		// turn class to sql
		SqlParamsPairs sqlAndParams = ModelSqlUtils.getGetFromObject(clazz, id);

		// query for list
		List<T> list = this.list(sqlAndParams.getSql(), sqlAndParams.getParams(), clazz);
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	// ---------------------------- update -----------------------------------//

	/**
	 * 更新某个对象 update object
	 * 
	 * @param po
	 * @throws Exception
	 */
	public void update(Object po) {
		SqlParamsPairs sqlAndParams = ModelSqlUtils.getUpdateFromObject(po);
		getProxy().update(sqlAndParams.getSql(), sqlAndParams.getParams());
	}

	/**
	 * 按字段更新某个对象
	 * 
	 * @author luheng
	 * @param o
	 * @param cols
	 * @throws Exception
	 */
	public void merge(Object po, String... cols) {
		if (null == cols || cols.length < 1)
			return;
		SqlParamsPairs sqlAndParams = ModelSqlUtils.getMergeFromObject(po, cols);
		getProxy().update(sqlAndParams.getSql(), sqlAndParams.getParams());
	}

	/**
	 * 批量执行更新操作
	 * 
	 * @param sql
	 * @param paramsList
	 */
	public void batchUpdate(String sql, List<Object[]> paramsList) {
		if (null == paramsList || paramsList.size() == 0) {
			getProxy().batchUpdate(sql);
		} else {
			BatchUpdateSetter batchUpdateSetter = new BatchUpdateSetter(paramsList);
			getProxy().batchUpdate(sql, batchUpdateSetter);
		}
	}

	/**
	 * 批量执行更新操作
	 * 
	 * @param sql
	 * @param paramsList
	 */
	public void batchUpdate(String sql) {
		this.batchUpdate(sql, null);
	}

	/**
	 * 保存对象的快捷方法 如果Id标定的是自增会将自增长的主键自动设置回对象 save object
	 * 
	 * @param po
	 * @throws Exception
	 */
	public void save(Object po) {
		String autoGeneratedColumnName = IdUtils.getAutoGeneratedId(po);
		if (!"".equals(autoGeneratedColumnName)) {
			// 有自增字段
			long idValue = save(po, autoGeneratedColumnName);
			// 把自增的主键值再设置回去
			IdUtils.setAutoIncreamentIdValue(po, autoGeneratedColumnName, idValue);
		} else {
			SqlParamsPairs sqlAndParams = ModelSqlUtils.getInsertFromObject(po);

			getProxy().update(sqlAndParams.getSql(), sqlAndParams.getParams());
		}
	}

	/**
	 * 保存对象并返回自增长主键的快捷方法
	 * 
	 * @param po
	 * @param autoGeneratedColumnName
	 *            自增长的主键的列名 比如 user_id
	 * @throws Exception
	 */
	public long save(Object po, String autoGeneratedColumnName) {
		SqlParamsPairs sqlAndParams = ModelSqlUtils.getInsertFromObject(po);
		// 动态切换库名
		String sql = sqlAndParams.getSql();
		return getProxy().insert(sql, sqlAndParams.getParams(), autoGeneratedColumnName);
	}

	// -------------------delete-----------------//
	public void delete(Object po) {
		if (null == po)
			return;
		SqlParamsPairs sqlAndParams = ModelSqlUtils.getDeleteFromObject(po);
		// 动态切换库名
		String sql = sqlAndParams.getSql();
		getProxy().update(sql, sqlAndParams.getParams());
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		try {
			ModelSqlUtils.setDriver(
					DatabaseDriver.fromJdbcUrl(jdbcTemplate.getDataSource().getConnection().getMetaData().getURL()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
