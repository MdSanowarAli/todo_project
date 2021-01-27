package com.proit.todoApi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date Range
 *
 */

public enum DateRangCriteria {

	TODAY("TODAY", "Today"), THIS_WEEK("THIS_WEEK", "This Week"), LAST_WEEK(
			"LAST_WEEK",
			"Last Week"), THIS_MONTH("THIS_MONTH", "This Month"), LAST_MONTH(
					"LAST_MONTH", "Last Month"), THIS_YEAR("THIS_YEAR",
							"This Year"), DATE_BETWEEN("DATE_BETWEEN",
									"Date Between");

	private final String value;
	private final String dateRange;

	DateRangCriteria(String value, String dateRange) {
		this.value = value;
		this.dateRange = dateRange;
	}

	public String getValue() {
		return value;
	}

	public String getDateRange() {
		return dateRange;
	}

	public static List<Map<String, Object>> getDateCriteriaList() {

		List<Map<String, Object>> dateCriteriaList = new ArrayList<Map<String, Object>>();

		for (DateRangCriteria r : DateRangCriteria.values()) {

			Map<String, Object> dateCriteriaMap = new HashMap<String, Object>();
			dateCriteriaMap.put("id", r.getValue());
			dateCriteriaMap.put("name", r.getDateRange());

			dateCriteriaList.add(dateCriteriaMap);
		}
		return dateCriteriaList;
	}

}
