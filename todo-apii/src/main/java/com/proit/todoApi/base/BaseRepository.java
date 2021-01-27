package com.proit.todoApi.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.dialect.Dialect;
import org.hibernate.jpa.QueryHints;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.proit.todoApi.pagination.DataTableRequest;
import com.proit.todoApi.pagination.PaginationCriteria;
import com.proit.todoApi.util.CommonFunctions;
import com.proit.todoApi.util.CommonUtils;
import com.proit.todoApi.util.Response;

public class BaseRepository implements CommonFunctions {

	private final Logger LOGGER = LoggerFactory.getLogger(BaseRepository.class);

	@PersistenceContext
	private EntityManager entityManager;

	public CriteriaBuilder builder = null;
	public CriteriaQuery criteria = null;
	public Root root = null;

	@Autowired
	private Environment env;

	public Response baseOnlySave(Object obj) {
		Response response = new Response();
		try {
			entityManager.persist(obj);
			response.setObj(obj);
			return getSuccessResponse("Saved Successfully", response);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return getErrorResponse("Save fail !!");
		}

	}

	public Response baseBatchOnlySave(List<Object> objects) {
		Response response = new Response();
		int batchSize = batchSize();
		try {
			List<Object> items = new ArrayList<Object>();
			for (int i = 0; i < objects.size(); i++) {
				if (i > 0 && i % batchSize == 0) {
					entityManager.flush();
					entityManager.clear();
				}
				Object object = objects.get(i);
				entityManager.persist(object);
				items.add(object);
			}
			response.setItems(items);
			return getSuccessResponse("Saved Successfully", response);
		} catch (Exception e) {
			e.printStackTrace();
			return getErrorResponse("Batch Save Fail !!");
		}
	}

	public Response baseSaveNFlush(Object obj) {
		Response response = new Response();

		try {
			entityManager.persist(obj);
			entityManager.flush();
			entityManager.clear();

			response.setObj(obj);
			return getSuccessResponse("Saved Successfully", response);
		} catch (Exception e) {
			e.printStackTrace();
			return getErrorResponse("Save Fail !!");
		}
	}

	public Response baseBatchSaveOrUpdate(List<Object> objects) {
		Response response = new Response();
		int batchSize = batchSize();
		try {
			List<Object> items = new ArrayList<Object>();
			for (int i = 0; i < objects.size(); i++) {
				if (i > 0 && i % batchSize == 0) {
					entityManager.flush();
					entityManager.clear();
				}
				Object object = objects.get(i);
				Object savedOrUpdateObject = entityManager.merge(object);
				items.add(savedOrUpdateObject);
			}
			response.setItems(items);
			return getSuccessResponse("Update Successfully", response);
		} catch (Exception e) {
			e.printStackTrace();
			return getErrorResponse("Batch Save Fail !!");
		}
	}

	public Response baseSaveOrUpdate(Object obj) {
		Response response = new Response();
		try {
			response.setObj(entityManager.merge(obj));
			return getSuccessResponse("Update Successfully", response);
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Save fail !!");
		}

	}

	public Response baseSaveOrUpdate1(Object obj) {
		Response response = new Response();
		try {
			entityManager.getTransaction().begin();
			response.setObj(entityManager.merge(obj));
			entityManager.getTransaction().commit();

			return getSuccessResponse("Update Successfully", response);
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Save fail !!");
		}

	}

	public Response baseUpdate(Object obj) {

		Response response = new Response();
		try {

			response.setObj(entityManager.merge(obj));

			return getSuccessResponse("Updated Successfully", response);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return getErrorResponse("Update fail !!");
		}

	}

	public Response baseRemove(Object obj) {

		try {
			entityManager.merge(obj);
			return getSuccessResponse("Remove Successfully");
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Remove fail !!");
		}

	}

	public Response baseDelete(Object obj) {
		try {
			entityManager.remove(obj);
			return getSuccessResponse("Delete Successfully");
		} catch (Exception e) {
			return getErrorResponse("Delete fail !!");
		}

	}

	public Response baseBatchDelete(String entityName, String columnName, @SuppressWarnings("rawtypes") List ids) {

		try {
			Query query = entityManager.createQuery("DELETE " + entityName + " WHERE " + columnName + " IN (:ids)");
			query.setParameter("ids", ids);
			query.executeUpdate();
			return getSuccessResponse("Deleted Successfully");
		} catch (Exception e) {
			return getErrorResponse("Delete fail !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseList(CriteriaQuery criteria) {
		Response response = new Response();
		List list = null;
		try {
			list = entityManager.createQuery(criteria).setHint(QueryHints.HINT_READONLY, true).getResultList();

			if (list.size() > 0) {
				response.setItems(list);
				return getSuccessResponse("Data found ", response);
			}

			return getSuccessResponse("Data Empty ");
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not found !!");
		}

	}

	@SuppressWarnings({ "rawtypes" })
	public Response baseTop1(CriteriaQuery criteria) {
		Response response = new Response();
		List list = new ArrayList<>();

		response = baseTopNList(criteria, 0);

		if (response.isSuccess()) {
			list = response.getItems();

			if (list == null || list.size() <= 0) {
				return getSuccessResponse("Data Empty ");
			}

			response.setItems(null);
			response.setObj(list.get(0));

			return getSuccessResponse("find data Successfully", response);

		} else {
			return getErrorResponse("Data not found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseTopNList(CriteriaQuery criteria, int topN) {

		Response response = new Response();
		List list = null;
		try {
			list = entityManager.createQuery(criteria).setFirstResult(topN).setHint(QueryHints.HINT_READONLY, true)
					.getResultList();

			if (list.size() > 0) {
				response.setItems(list);
				return getSuccessResponse("Data found ", response);
			}

			return getSuccessResponse("Data Empty ");
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not found !!");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseNotReadOnlyList(CriteriaQuery criteria) {
		Response response = new Response();
		List list = null;
		try {
			list = entityManager.createQuery(criteria).getResultList();

			if (list.size() > 0) {
				response.setItems(list);
				return getSuccessResponse("Data found ", response);
			}

			return getSuccessResponse("Data Empty ");
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not found !!");
		}

	}

	// TODO
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseDataList(CriteriaQuery criteria) {
		Response response = new Response();
		List list = null;
		try {
			list = entityManager.createQuery(criteria).getResultList();

			if (list.size() > 0) {
				response.setItems(list);
				return getSuccessResponse("Data found ", response);
			}

			return getSuccessResponse("Data Empty ");
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseTupleList(Map<String, String> columnMap, CriteriaQuery criteria) {
		Response response = new Response();
		List list = null;
		try {
			list = entityManager.createQuery(criteria).setHint(QueryHints.HINT_READONLY, true).getResultList();

			if (list.size() > 0) {

				List itemList = new ArrayList<>();

				List<Tuple> tupleList = list;

				for (Tuple tuple : tupleList) {

					Map<String, Object> tupleMap = new HashMap<String, Object>();
					int index = 0;

					for (Map.Entry<String, String> columName : columnMap.entrySet()) {

						tupleMap.put(columName.getValue(), tuple.get(index));

						index++;

					}

					itemList.add(tupleMap);

				}
				response.setItems(itemList);
				return getSuccessResponse("Data found ", response);
			}

			return getSuccessResponse("Data Empty ");
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseFindByParent(CriteriaQuery criteria) {
		Response response = new Response();
		List list = null;
		try {
			list = entityManager.createQuery(criteria).setHint(QueryHints.HINT_READONLY, true).getResultList();

			if (list.size() > 0) {

				response.setItems(list);
				return getSuccessResponse("Data found ", response);
			}

			return getSuccessResponse("Data Empty ");
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseListByDistinctHint(CriteriaQuery criteria) {
		Response response = new Response();
		List list = null;

		try {

			list = entityManager.createQuery(criteria).setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false)
					.getResultList();

			if (list.size() > 0) {

				response.setItems(list);
				return getSuccessResponse("Data found ", response);
			}

			return getSuccessResponse("Data Empty ");

		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not found !!");
		}

	}

	@SuppressWarnings({ "rawtypes" })
	public Response baseList(TypedQuery typedQuery) {
		Response response = new Response();
		List list = null;

		try {

			list = typedQuery.setHint(QueryHints.HINT_READONLY, true).getResultList();

			if (list.size() > 0) {

				response.setItems(list);
				return getSuccessResponse("Data found ", response);
			}

			return getSuccessResponse("Data Empty ");

		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseFindById(CriteriaQuery criteria) {
		Response response = new Response();
		Object obj = null;
		try {
			obj = entityManager.createQuery(criteria).getSingleResult();
			response.setObj(obj);
			return getSuccessResponse("find data Successfully", response);
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not Found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseFindByIdReadOnly(CriteriaQuery criteria) {
		Response response = new Response();
		Object obj = null;
		try {

			obj = entityManager.createQuery(criteria).setHint(QueryHints.HINT_READONLY, true).getSingleResult();
			response.setObj(obj);
			return getSuccessResponse("find data Successfully", response);
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not Found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response baseSingleObject(CriteriaQuery criteria) {
		Response response = new Response();
		Object obj = null;
		try {
			obj = entityManager.createQuery(criteria).getSingleResult();
			response.setObj(obj);
			return getSuccessResponse("find data Successfully", response);
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not Found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> TypedQuery baseTypedQuery(CriteriaQuery criteria, DataTableRequest dataTableInRQ) {

		CriteriaQuery<T> select = criteria.select(root);

		TypedQuery<T> typedQuery = entityManager.createQuery(select);
		typedQuery.setFirstResult(dataTableInRQ.getStart());
		typedQuery.setMaxResults(dataTableInRQ.getLength());

		return typedQuery;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> TypedQuery baseTypedQueryById(CriteriaQuery criteria, DataTableRequest dataTableInRQ) {

		CriteriaQuery<T> select = criteria.select(root.get("id"));

		TypedQuery<T> typedQuery = entityManager.createQuery(select);
		typedQuery.setFirstResult(dataTableInRQ.getStart());
		typedQuery.setMaxResults(dataTableInRQ.getLength());

		return typedQuery;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> TypedQuery baseTypedQuery(CriteriaQuery criteria, int start, int length) {

		CriteriaQuery<T> select = criteria.select(root);

		TypedQuery<T> typedQuery = entityManager.createQuery(select);
		typedQuery.setFirstResult(start);
		typedQuery.setMaxResults(length);

		return typedQuery;
	}

//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public Long baseFilterCountTypedQuery(CriteriaQuery criteria) {
//
//		Long totalRowCount = (Long) entityManager.createQuery(criteria).getSingleResult();
//
//		return totalRowCount;
//	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> TypedQuery baseTypedQuery(CriteriaQuery criteria) {

		CriteriaQuery<T> select = criteria.select(root);
		TypedQuery<T> typedQuery = entityManager.createQuery(select);

		return typedQuery;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response getListFindById(CriteriaQuery criteria) {
		Response response = new Response();
		Object obj = null;
		try {
			obj = entityManager.createQuery(criteria).getResultList();
			response.setItems((List) obj);
			return getSuccessResponse("find data Successfully", response);
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not Found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response getListFindByIdReadonly(CriteriaQuery criteria) {
		Response response = new Response();
		Object obj = null;
		try {

			obj = entityManager.createQuery(criteria).setHint(QueryHints.HINT_READONLY, true).getResultList();
			response.setItems((List) obj);
			return getSuccessResponse("find data Successfully", response);
		} catch (Exception e) {
			// TODO: handle exception
			return getErrorResponse("Data not Found !!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> entityManagerBuilderCriteriaQueryRoot(Class clazz) {

		Map<String, Object> entityManagerParams = new HashMap<String, Object>();

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery criteria = builder.createQuery(clazz);
		entityManagerParams.put("builder", builder);
		entityManagerParams.put("criteria", criteria);
		entityManagerParams.put("root", root);

		return entityManagerParams;

	}

	@SuppressWarnings({ "rawtypes" })
	public void initEntityManagerBuilderCriteriaQueryRoot(Class clazz) {
		criteriaRoot(clazz);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Root criteriaRoot(Class clazz) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery criteria = builder.createQuery(clazz);
		Root root = criteria.from(clazz);
		this.builder = builder;
		this.criteria = criteria;
		this.root = root;

		return root;
	}

	public <T> void totalCriteriaQuery(Class<T> clazz) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<T> root = criteria.from(clazz);

		this.builder = builder;
		this.criteria = criteria;
		this.root = root;

	}

	public CriteriaBuilder criteriaBuilder() {
		return entityManager.getCriteriaBuilder();
	}

	public CriteriaQuery<Long> longCriteriaQuery(CriteriaBuilder builder) {
		return builder.createQuery(Long.class);
	}

	public CriteriaQuery<Date> dateCriteriaQuery(CriteriaBuilder builder) {
		return builder.createQuery(Date.class);
	}

	public CriteriaQuery<Tuple> baseCiteriaQueryTuple(CriteriaBuilder builder) {
		return builder.createTupleQuery();
	}

	public <T> Root<T> from(Class<T> clazz, CriteriaQuery<Long> criteria) {
		return criteria.from(clazz);
	}

	public <T> Root<T> fromDate(Class<T> clazz, CriteriaQuery<Date> criteria) {
		return criteria.from(clazz);
	}

	public <T> Root<T> baseFrom(Class<T> clazz, CriteriaQuery<Tuple> criteria) {
		return criteria.from(clazz);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Root LongCriteriaQuery(Class clazz) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root root = criteria.from(clazz);
		this.builder = builder;
		this.criteria = criteria;
		this.root = root;
		return root;

	}

	/*
	 * @SuppressWarnings({ "rawtypes", "unchecked" }) public <T> Long
	 * totalCount(Class<T> clazz) {
	 *
	 * CriteriaBuilder builder = entityManager.getCriteriaBuilder(); CriteriaQuery
	 * criteria = builder.createQuery(Long.class);
	 * criteria.select(builder.count(criteria.from(clazz))); Long totalRowCount =
	 * (Long)entityManager.createQuery(criteria).getSingleResult();
	 *
	 * return totalRowCount;
	 *
	 * }
	 */

	public <T> String criteriaQuery(Class<T> clazz) {
		CriteriaQuery<T> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(clazz);
		Root<T> root = criteriaQuery.from(clazz);
		return criteriaQuery.select(root).toString();
	}

	public <T> void limitedCriteriaQuer(Class clazz) {
		Query limitedCriteriaQuery = entityManager.createQuery(criteriaQuery(clazz)).setFirstResult(0)
				.setMaxResults(10);
		// return limitedCriteriaQuery.getResultList();
	}

	/*
	 * public <T> Long totalCount(CriteriaBuilder builder, CriteriaQuery<Long>
	 * criteria, List<Predicate> p) {
	 *
	 * //CriteriaBuilder builder = entityManager.getCriteriaBuilder();
	 * //CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
	 *
	 * // criteria.select(builder.count(criteria.from(clazz)));
	 *
	 * if (!CollectionUtils.isEmpty(p)) {
	 *
	 * Predicate[] pArray = p.toArray(new Predicate[] {}); Predicate predicate =
	 * builder.and(pArray); criteria.where(predicate); }
	 *
	 *
	 * Long totalRowCount = entityManager.createQuery(criteria).getSingleResult();
	 *
	 * return totalRowCount;
	 *
	 * }
	 */

	public <T> Long totalCount(Class<T> clazz, List<Predicate> p) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

		criteria.select(builder.count(criteria.from(clazz)));

		/*
		 * if (!CollectionUtils.isEmpty(p)) {
		 *
		 * Predicate[] pArray = p.toArray(new Predicate[] {}); Predicate predicate =
		 * builder.and(pArray); criteria.where(predicate); }
		 */

		Long totalRowCount = entityManager.createQuery(criteria).getSingleResult();

		return totalRowCount;

	}

	public <T> Long totalCount(CriteriaBuilder builder, CriteriaQuery<Long> criteria, Root<T> root,
			List<Predicate> pConjunction) {

		criteria.select(builder.count(root));

		if (!CollectionUtils.isEmpty(pConjunction)) {
			Predicate[] pArray = pConjunction.toArray(new Predicate[] {});
			Predicate predicate = builder.and(pArray);
			criteria.where(predicate);
		}

		Long totalRowCount = entityManager.createQuery(criteria).getSingleResult();

		return totalRowCount;

	}

	public <T> CriteriaQuery<Tuple> baseMultiSeletCriteria(CriteriaBuilder builder, CriteriaQuery<Tuple> criteria,
			Root<T> root, Map<String, String> columnNameMap, List<Predicate> pConjunction) {

		if (!CollectionUtils.isEmpty(pConjunction)) {
			Predicate[] pArray = pConjunction.toArray(new Predicate[] {});
			Predicate predicate = builder.and(pArray);
			criteria.where(predicate);
		}

		List<Selection<?>> columnSelection = new LinkedList<Selection<?>>();

		for (Map.Entry<String, String> columName : columnNameMap.entrySet()) {
			columnSelection.add(root.get(columName.getValue()));
		}

		criteria.multiselect(columnSelection);

		return criteria;

	}

	public <T> CriteriaQuery<Tuple> baseMultiSeletCriteria(CriteriaBuilder builder, CriteriaQuery<Tuple> criteria,
			Root<T> root, Map<String, String> columnNameMap, List<Predicate> pConjunction,
			List<Predicate> pDisJunction) {

		List<Predicate> pArrayJoin = new ArrayList<Predicate>();

		Predicate predicateAND = null;
		Predicate predicateOR = null;

		if (!CollectionUtils.isEmpty(pConjunction)) {
			Predicate[] pArray = pConjunction.toArray(new Predicate[] {});
			predicateAND = builder.and(pArray);

		}

		if (!CollectionUtils.isEmpty(pDisJunction)) {
			Predicate[] pArray = pDisJunction.toArray(new Predicate[] {});
			predicateOR = builder.or(pArray);
		}

		if (predicateAND != null) {
			pArrayJoin.add(predicateAND);
		}

		if (predicateOR != null) {
			pArrayJoin.add(predicateOR);
		}

		criteria.where(pArrayJoin.toArray(new Predicate[0]));

		List<Selection<?>> columnSelection = new LinkedList<Selection<?>>();

		for (Map.Entry<String, String> columName : columnNameMap.entrySet()) {

			if (columName.getKey().equals("unitNo")) {
				root.join(columName.getValue(), JoinType.LEFT);
			}

			if (columName.getKey().equals("serviceCategory")) {
				root.join(columName.getValue(), JoinType.LEFT);
			}

			if (columName.getKey().equals("rankNo")) {
				root.join(columName.getValue(), JoinType.LEFT);
			}

			columnSelection.add(root.get(columName.getValue()));
		}

		criteria.multiselect(columnSelection);

		return criteria;

	}

	public <T> CriteriaQuery<Tuple> baseDistinctMultiSeletCriteria(CriteriaBuilder builder,
			CriteriaQuery<Tuple> criteria, Root<T> root, Map<String, String> columnNameMap,
			List<Predicate> pConjunction) {

		if (!CollectionUtils.isEmpty(pConjunction)) {
			Predicate[] pArray = pConjunction.toArray(new Predicate[] {});
			Predicate predicate = builder.and(pArray);
			criteria.where(predicate);
		}

		List<Selection<?>> columnSelection = new LinkedList<Selection<?>>();

		for (Map.Entry<String, String> columName : columnNameMap.entrySet()) {
			columnSelection.add(root.get(columName.getValue()));
		}

		criteria.multiselect(columnSelection).distinct(true);

		return criteria;

	}

	@SuppressWarnings("unchecked")
	public Long maxValue(CriteriaBuilder builder, CriteriaQuery<Long> criteria, @SuppressWarnings("rawtypes") Root root,
			List<Predicate> pConjunction, String columnName) {

		criteria.select(builder.max(root.get(columnName)));

		if (!CollectionUtils.isEmpty(pConjunction)) {
			Predicate[] pArray = pConjunction.toArray(new Predicate[] {});
			Predicate predicate = builder.and(pArray);
			criteria.where(predicate);
		}

		Long maxVal = entityManager.createQuery(criteria).getSingleResult();

		return maxVal;

	}

	@SuppressWarnings("unchecked")
	public Date maxDate(CriteriaBuilder builder, CriteriaQuery<Date> criteria, @SuppressWarnings("rawtypes") Root root,
			List<Predicate> pConjunction, String columnName) {

		criteria.select(builder.max(root.get(columnName)));

		if (!CollectionUtils.isEmpty(pConjunction)) {
			Predicate[] pArray = pConjunction.toArray(new Predicate[] {});
			Predicate predicate = builder.and(pArray);
			criteria.where(predicate);
		}

		Date maxDate = entityManager.createQuery(criteria).getSingleResult();

		return maxDate;

	}

	public <T> Long filterTotalCount(CriteriaBuilder builder, CriteriaQuery<Long> criteria, Root<T> root,
			List<Predicate> pArrayJoin) {

		criteria.select(builder.count(root));

		criteria.where(pArrayJoin.toArray(new Predicate[0]));

		Long totalRowCount = entityManager.createQuery(criteria).getSingleResult();

		return totalRowCount;

	}

	public <T> Long totalCount(CriteriaBuilder builder, CriteriaQuery<Long> criteria, Root<T> root,
			List<Predicate> pConjunction, List<Predicate> pDisjunction) {

		List<Predicate> pArrayJoin = new ArrayList<Predicate>();
		Predicate predicateAND = null;
		Predicate predicateOR = null;

		if (!CollectionUtils.isEmpty(pConjunction)) {
			Predicate[] pArray = pConjunction.toArray(new Predicate[] {});
			predicateAND = builder.and(pArray);
		}
		if (!CollectionUtils.isEmpty(pDisjunction)) {
			Predicate[] pArray = pDisjunction.toArray(new Predicate[] {});
			predicateOR = builder.or(pArray);
		}
		if (predicateAND != null) {
			pArrayJoin.add(predicateAND);
		}
		if (predicateOR != null) {
			pArrayJoin.add(predicateOR);
		}

		criteria.where(pArrayJoin.toArray(new Predicate[0]));

		criteria.select(builder.count(root));

		Long totalRowCount = entityManager.createQuery(criteria).getSingleResult();

		return totalRowCount;

	}

	@SuppressWarnings("unchecked")
	public <T> List<Predicate> dataTablefilter(DataTableRequest<T> dataTableInRQ) {

		PaginationCriteria paginationCriteria = dataTableInRQ.getPaginationRequest();
		paginationCriteria.getFilterBy().getMapOfFilters();

		List<Predicate> p = new ArrayList<Predicate>();

		if (!paginationCriteria.isFilterByEmpty()) {
			Iterator<Entry<String, String>> fbit = paginationCriteria.getFilterBy().getMapOfFilters().entrySet()
					.iterator();

			while (fbit.hasNext()) {
				Map.Entry<String, String> pair = fbit.next();
				if (!pair.getKey().equals("ssModifiedOn")) {
					p.add(builder.like(builder.lower(root.get(pair.getKey())),
							CommonUtils.PERCENTAGE_SIGN + pair.getValue().toLowerCase() + CommonUtils.PERCENTAGE_SIGN));
				}

			}

		}

		return p;

	}

	@SuppressWarnings("unchecked")
	public <T> List<Predicate> dataTablefilter(DataTableRequest<T> dataTableInRQ, Class clazz) {

		PaginationCriteria paginationCriteria = dataTableInRQ.getPaginationRequest();
		paginationCriteria.getFilterBy().getMapOfFilters();

		List<Predicate> p = new ArrayList<Predicate>();

		if (!paginationCriteria.isFilterByEmpty()) {
			Iterator<Entry<String, String>> fbit = paginationCriteria.getFilterBy().getMapOfFilters().entrySet()
					.iterator();
			Field[] fields = getClassFields(clazz);

			while (fbit.hasNext()) {
				Map.Entry<String, String> pair = fbit.next();

				String dataType = getClassFieldDataType(fields, pair.getKey());

				if (dataType != null && dataType.equals("class java.lang.String")) {
					p.add(builder.like(builder.lower(root.get(pair.getKey())),
							CommonUtils.PERCENTAGE_SIGN + pair.getValue().toLowerCase() + CommonUtils.PERCENTAGE_SIGN));
				}

			}

		}

		return p;

	}

	public Field[] getClassFields(Class claz) {
		return claz.getDeclaredFields();
	}

	public String getClassFieldDataType(Field[] fields, String fieldName) {

		String fieldType = "";

		for (Field field : fields) {

			if (field.getName().equals(fieldName)) {

				return fieldType = field.getType().toString();
			}

		}

		return null;

	}

	public <T> List<Predicate> dataTablefilter(DataTableRequest<T> dataTableInRQ, CriteriaBuilder builder, Root root) {

		PaginationCriteria paginationCriteria = dataTableInRQ.getPaginationRequest();
		paginationCriteria.getFilterBy().getMapOfFilters();

		List<Predicate> p = new ArrayList<Predicate>();

		if (!paginationCriteria.isFilterByEmpty()) {
			Iterator<Entry<String, String>> fbit = paginationCriteria.getFilterBy().getMapOfFilters().entrySet()
					.iterator();

			while (fbit.hasNext()) {
				Map.Entry<String, String> pair = fbit.next();
				if (!pair.getKey().equals("ssModifiedOn")) {

					// System.out.println("pair.getKey() " + pair.getKey());

					p.add(builder.like(builder.lower(root.get(pair.getKey())),
							CommonUtils.PERCENTAGE_SIGN + pair.getValue().toLowerCase() + CommonUtils.PERCENTAGE_SIGN));
				}

			}

		}

		return p;

	}

	public <T> List<Predicate> dataTablefilter(DataTableRequest<T> dataTableInRQ, CriteriaBuilder builder, Root root,
			Class clazz) {

		PaginationCriteria paginationCriteria = dataTableInRQ.getPaginationRequest();
		paginationCriteria.getFilterBy().getMapOfFilters();

		List<Predicate> p = new ArrayList<Predicate>();

		if (!paginationCriteria.isFilterByEmpty()) {
			Iterator<Entry<String, String>> fbit = paginationCriteria.getFilterBy().getMapOfFilters().entrySet()
					.iterator();
			Field[] fields = getClassFields(clazz);

			while (fbit.hasNext()) {
				Map.Entry<String, String> pair = fbit.next();

				String dataType = getClassFieldDataType(fields, pair.getKey());

				if (dataType != null && dataType.equals("class java.lang.String")) {
					p.add(builder.like(builder.lower(root.get(pair.getKey())),
							CommonUtils.PERCENTAGE_SIGN + pair.getValue().toLowerCase() + CommonUtils.PERCENTAGE_SIGN));
				}

			}

		}

		return p;

	}

	@SuppressWarnings("unchecked")
	public <T> List<Predicate> basePredicate(Map<String, Object> fields, CriteriaBuilder builder, Root root) {

		List<Predicate> p = new ArrayList<Predicate>();

		Iterator<Entry<String, Object>> fv = fields.entrySet().iterator();

		while (fv.hasNext()) {
			Map.Entry<String, Object> pair = fv.next();

			if (pair.getValue() instanceof String) {
				p.add(builder.like(builder.lower(root.get(pair.getKey())), CommonUtils.PERCENTAGE_SIGN
						+ ((String) pair.getValue()).toLowerCase() + CommonUtils.PERCENTAGE_SIGN));
			}

			if (pair.getValue() instanceof Long) {
				p.add(builder.equal(root.get(pair.getKey()), pair.getValue()));
			}

		}

		return p;

	}

	@SuppressWarnings("unchecked")
	public <T> List<Predicate> basePredicateLeftExact(Map<String, Object> fields, CriteriaBuilder builder, Root root) {

		List<Predicate> p = new ArrayList<Predicate>();

		Iterator<Entry<String, Object>> fv = fields.entrySet().iterator();

		while (fv.hasNext()) {
			Map.Entry<String, Object> pair = fv.next();

			if (pair.getValue() instanceof String) {
				p.add(builder.like(builder.lower(root.get(pair.getKey())),
						((String) pair.getValue()).toLowerCase() + CommonUtils.PERCENTAGE_SIGN));
			}

			if (pair.getValue() instanceof Long) {
				p.add(builder.equal(root.get(pair.getKey()), pair.getValue()));
			}

		}

		return p;

	}

	@SuppressWarnings("unchecked")
	public <T> List<Predicate> basePredicateRightExact(Map<String, Object> fields, CriteriaBuilder builder, Root root) {

		List<Predicate> p = new ArrayList<Predicate>();

		Iterator<Entry<String, Object>> fv = fields.entrySet().iterator();

		while (fv.hasNext()) {
			Map.Entry<String, Object> pair = fv.next();

			if (pair.getValue() instanceof String) {
				p.add(builder.like(builder.lower(root.get(pair.getKey())),
						CommonUtils.PERCENTAGE_SIGN + ((String) pair.getValue()).toLowerCase()));
			}

			if (pair.getValue() instanceof Long) {
				p.add(builder.equal(root.get(pair.getKey()), pair.getValue()));
			}

		}

		return p;

	}

	@SuppressWarnings("unchecked")
	public <T> List<Predicate> basePredicateExact(Map<String, Object> fields, CriteriaBuilder builder, Root root) {

		List<Predicate> p = new ArrayList<Predicate>();

		Iterator<Entry<String, Object>> fv = fields.entrySet().iterator();

		while (fv.hasNext()) {
			Map.Entry<String, Object> pair = fv.next();

			if (pair.getValue() instanceof String) {
				p.add(builder.like(builder.lower(root.get(pair.getKey())), ((String) pair.getValue()).toLowerCase()));
			}

			if (pair.getValue() instanceof Long) {
				p.add(builder.equal(root.get(pair.getKey()), pair.getValue()));
			}

		}

		return p;

	}

	@SuppressWarnings("unchecked")
	public <T> List<Predicate> basePredicate(Map<String, Object> fields) {

		List<Predicate> p = new ArrayList<Predicate>();

		Iterator<Entry<String, Object>> fv = fields.entrySet().iterator();

		while (fv.hasNext()) {
			Map.Entry<String, Object> pair = fv.next();

			if (pair.getValue() instanceof String) {
				p.add(builder.like(builder.lower(root.get(pair.getKey())), CommonUtils.PERCENTAGE_SIGN
						+ ((String) pair.getValue()).toLowerCase() + CommonUtils.PERCENTAGE_SIGN));
			}

			if (pair.getValue() instanceof Long) {
				p.add(builder.equal(root.get(pair.getKey()), pair.getValue()));
			}

		}

		return p;

	}

	@SuppressWarnings({ "rawtypes" })
	public <T> TypedQuery typedQuery(List<Predicate> pConjunctionParam, List<Predicate> pDisJunctionParam) {

		List<Predicate> pArrayJoin = new ArrayList<Predicate>();

		List<Predicate> pConjunction = pConjunctionParam;
		List<Predicate> pDisJunction = pDisJunctionParam;

		Predicate predicateAND = null;
		Predicate predicateOR = null;

		if (!CollectionUtils.isEmpty(pConjunction)) {
			Predicate[] pArray = pConjunction.toArray(new Predicate[] {});
			predicateAND = builder.and(pArray);
		}
		if (!CollectionUtils.isEmpty(pDisJunction)) {
			Predicate[] pArray = pDisJunction.toArray(new Predicate[] {});
			predicateOR = builder.or(pArray);
		}
		if (predicateAND != null) {
			pArrayJoin.add(predicateAND);
		}
		if (predicateOR != null) {
			pArrayJoin.add(predicateOR);
		}
		criteria.where(pArrayJoin.toArray(new Predicate[0]));

		return baseTypedQuery(criteria);
	}

	public String storedProcedureGenerateId(Long companyNo, String prefix) {
		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("K_GENERAL.PD_GENARATE_ID")

				.registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(2, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(3, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(4, Long.class, ParameterMode.IN)
				.registerStoredProcedureParameter(5, String.class, ParameterMode.OUT)

				.setParameter(1, prefix).setParameter(2, "OPD_REGISTRATION").setParameter(3, "HOSPITAL_NUMBER")
				.setParameter(4, companyNo);

		return (String) query.getOutputParameterValue(5);
	}

	public String storedProcedureGenerateId(Long companyNo, String prefix, String tableName, String columnName) {
		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("K_GENERAL.PD_GENARATE_ID")

				.registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(2, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(3, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(4, Long.class, ParameterMode.IN)
				.registerStoredProcedureParameter(5, String.class, ParameterMode.OUT)

				.setParameter(1, prefix).setParameter(2, tableName).setParameter(3, columnName)
				.setParameter(4, companyNo);

		return (String) query.getOutputParameterValue(5);
	}

	public Long storedProcedureGenerateNo(Long companyNo, String sequenceName) {

		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("K_GENERAL.PD_GENARATE_NO")

				.registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(2, Long.class, ParameterMode.IN)
				.registerStoredProcedureParameter(3, Long.class, ParameterMode.OUT)
				.registerStoredProcedureParameter(4, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(5, Long.class, ParameterMode.IN)

				.setParameter(1, sequenceName).setParameter(2, companyNo).setParameter(4, "YY").setParameter(5, 10l);
		try {
			query.execute();
		} catch (Exception e) {
			System.err.println("sequenceName: " + sequenceName);
		}

		return (Long) query.getOutputParameterValue(3);
	}

	public String storedProcedureAdmissionId(String admissionCategory, Long companyNo) {

		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("pd_genarate_ipd_admission_id")

				.registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(2, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(3, String.class, ParameterMode.IN)
				.registerStoredProcedureParameter(4, Long.class, ParameterMode.IN)
				.registerStoredProcedureParameter(5, String.class, ParameterMode.OUT)

				.setParameter(1, admissionCategory).setParameter(2, "IPD_ADMISSION").setParameter(3, "ADMISSION_ID")
				.setParameter(4, companyNo);

		try {
			query.execute();
		} catch (Exception e) {
			System.err.println(e);
		}

		return (String) query.getOutputParameterValue(5);
	}

	/**
	 * @param companyNo
	 * @param tableName
	 * @param columnName
	 * @param dataLength
	 * @return
	 */
	public Long functionFdAutoNo(Long companyNo, String tableName, String columnName, Long dataLength) {
		BigDecimal maxValue = null;
		maxValue = (BigDecimal) entityManager
				.createNativeQuery("SELECT FD_AUTO_NO(:pTable,:pColumn,:pCompanyNo,:pDataLength) FROM DUAL")
				.setParameter("pTable", tableName).setParameter("pColumn", columnName)
				.setParameter("pCompanyNo", companyNo).setParameter("pDataLength", dataLength).getSingleResult();

		if (maxValue == null) {
			return null;
		}
		return maxValue.longValue();

	}

	/**
	 * @param companyNo
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	public Long functionFdAutoNo(Long companyNo, String tableName, String columnName) {
		return functionFdAutoNo(companyNo, tableName, columnName, 6L);

	}

	public Connection getOraConnection() {

//    	Connection connection = null;
//    	   
//           try {
//        	   EntityManagerFactoryInfo info = (EntityManagerFactoryInfo)entityManager.getEntityManagerFactory();
//        	   connection = info.getDataSource().getConnection();
// 
//         } catch (SQLException e) {
//             System.out.println("Connection Failed! Check output console");
//         }
//    	   
//    	   if (connection != null) {
//             return connection;
//         } else {
//             System.out.println("Failed to make connection!");
//             return null;
//         }

		try {

			Class.forName(env.getProperty("spring.datasource.driver-class-name"));

		} catch (ClassNotFoundException e) {
			System.out.println("Where is your Oracle JDBC Driver?");

		}
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(env.getProperty("spring.datasource.url"),
					env.getProperty("spring.datasource.username"), env.getProperty("spring.datasource.password"));

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
		}
		if (connection != null) {
			return connection;
		} else {
			System.out.println("Failed to make connection!");
			return null;
		}

//        try {
//        	
//        	Class.forName(env.getProperty("ora.driver"));
//        	
//        } catch (ClassNotFoundException e) {
//        	System.out.println("Where is your Oracle JDBC Driver?");
//        	
//        }
//        Connection connection = null;
//        try {
//        	connection = DriverManager.getConnection(env.getProperty("ora.url"), env.getProperty("ora.user"),env.getProperty("ora.password"));
//        	
//        } catch (SQLException e) {
//        	System.out.println("Connection Failed! Check output console");
//        }
//        if (connection != null) {
//        	return connection;
//        } else {
//        	System.out.println("Failed to make connection!");
//        	return null;
//        }

	}

	public void finalyConStmRs(Connection con, Statement stm, ResultSet rs) {

		try {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
			if (con != null) {
				con.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void finallyOutputStream(ByteArrayOutputStream baos) {

		if (baos != null) {
			try {
				baos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void finalyConPstmRs(Connection con, PreparedStatement pstm, ResultSet rs) {

		try {
			if (rs != null) {
				rs.close();
			}
			if (pstm != null) {
				pstm.close();
			}
			if (con != null) {
				con.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void finalyStmRs(Statement stm, ResultSet rs) {

		try {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void finalyRs(ResultSet rs) {

		try {
			if (rs != null) {
				rs.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String[] jsonArryToStringQuotsArry(JSONArray arr) {
		String[] item = new String[arr.length()];
		for (int i = 0; i < arr.length(); ++i) {
			item[i] = "'" + arr.optString(i) + "'";
		}
		return item;
	}

	@SuppressWarnings("unused")
	private String[] jsonArryToStringArry(JSONArray arr) {
		String[] item = new String[arr.length()];
		for (int i = 0; i < arr.length(); ++i) {
			item[i] = arr.optString(i);
		}
		return item;
	}

	@SuppressWarnings("unused")
	private int[] jsonArryToIntArry(JSONArray arr) {
		int[] item = new int[arr.length()];
		for (int i = 0; i < arr.length(); ++i) {
			item[i] = arr.optInt(i);
		}
		return item;
	}

	@SuppressWarnings("unused")
	private Float[] jsonArryToFloatArry(JSONArray arr) {
		Float[] item = new Float[arr.length()];
		for (int i = 0; i < arr.length(); ++i) {
			item[i] = arr.optFloat(i);
		}
		return item;
	}
	
	protected int batchSize() {
		return Integer.valueOf(Dialect.DEFAULT_BATCH_SIZE);
	}
	
	public Long generateTodoNo(Long no) {
		return functionFdAutoNo(no, "to_do", "todo_no", 9L);
	}
}
