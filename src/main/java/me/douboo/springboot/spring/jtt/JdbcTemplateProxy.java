package me.douboo.springboot.spring.jtt;

import static me.douboo.springboot.spring.jtt.utils.CatalogUtils.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import me.douboo.springboot.spring.jtt.impl.ReturnIdPreparedStatementCreator;

public class JdbcTemplateProxy {

	private JdbcTemplate jdbcTemplate;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// private static String changIn

	// ------------- select -----------------//

	public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		// dynamic change catalog name
		sql = changeCatalog(sql);
		try {
			return jdbcTemplate.query(sql, rowMapper);
		} catch (DataAccessException e) {
			logger.error("Error SQL: " + sql);
			throw e;
		}
	}

	public <T> List<T> query(String sql, Object[] params, RowMapper<T> rowMapper) throws DataAccessException {
		// dynamic change catalog name
		sql = changeCatalog(sql);
		try {
			return jdbcTemplate.query(sql, params, rowMapper);
		} catch (DataAccessException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object p : params) {
				sb.append(p + " | ");
			}
			sb.append("]");
			logger.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}
	}

	public Map<String, Object> queryForMap(String sql) throws DataAccessException {
		// dynamic change catalog name
		sql = changeCatalog(sql);
		try {
			return jdbcTemplate.queryForMap(sql);
		} catch (DataAccessException e) {
			logger.error("Error SQL: " + sql);
			throw e;
		}
	}

	public Map<String, Object> queryForMap(String sql, Object[] params) throws DataAccessException {
		// dynamic change catalog name
		sql = changeCatalog(sql);
		try {
			return jdbcTemplate.queryForMap(sql, params);
		} catch (DataAccessException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object p : params) {
				sb.append(p + " | ");
			}
			sb.append("]");
			logger.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}
	}

	// ------------ update ------------------//
	public int update(String sql, Object[] params) throws DataAccessException {
		// dynamic change catalog name
		sql = changeCatalog(sql);
		try {
			return jdbcTemplate.update(sql, params);
		} catch (DataAccessException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object p : params) {
				sb.append(p + " | ");
			}
			sb.append("]");
			logger.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}
	}

	public int[] batchUpdate(String sql, final BatchPreparedStatementSetter pss) throws DataAccessException {
		// dynamic change catalog name
		sql = changeCatalog(sql);
		try {
			return jdbcTemplate.batchUpdate(sql, pss);
		} catch (DataAccessException e) {
			logger.error("Error SQL: " + sql);
			throw e;
		}
	}

	public int batchUpdate(String sql) throws DataAccessException {
		// dynamic change catalog name
		sql = changeCatalog(sql);
		try {
			return jdbcTemplate.update(sql);
		} catch (DataAccessException e) {
			logger.error("Error SQL: " + sql);
			throw e;
		}
	}

	/**
	 * insert a row with auto increament id
	 * 
	 * @param sql
	 * @param params
	 * @param autoGeneratedColumnName
	 * @return
	 * @throws DataAccessException
	 */
	public long insert(String sql, Object[] params, String autoGeneratedColumnName) throws DataAccessException {
		// dynamic change catalog name
		sql = changeCatalog(sql);

		ReturnIdPreparedStatementCreator psc = new ReturnIdPreparedStatementCreator(sql, params, autoGeneratedColumnName);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			jdbcTemplate.update(psc, keyHolder);
		} catch (DataAccessException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (int i = 0; i < params.length; i++) {
				Object p = params[i];
				if (p instanceof String) {
					sb.append("'").append(p).append("'");
				} else {
					sb.append(p);
				}
				if (i < params.length - 1) {
					sb.append(" , ");
				}
			}
			sb.append("]");
			logger.error("Error SQL: " + sql + " Params: " + sb.toString());
			throw e;
		}

		return keyHolder.getKey().longValue();
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
