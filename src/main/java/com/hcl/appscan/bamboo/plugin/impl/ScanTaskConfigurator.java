/**
 * (c) Copyright IBM Corporation 2016.
 * (c) Copyright HCL Technologies Ltd. 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.bamboo.plugin.impl;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.atlassian.sal.api.component.ComponentLocator;
import com.hcl.appscan.bamboo.plugin.util.Utility;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.scanners.dynamic.DASTConstants;
import com.hcl.appscan.sdk.scanners.sast.SASTConstants;
import org.apache.commons.lang3.StringUtils;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRequirementSupport;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import com.atlassian.bamboo.utils.i18n.I18nBeanFactory;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

public class ScanTaskConfigurator extends AbstractTaskConfigurator implements TaskRequirementSupport, IScannerConstants {
	
	private static final String CRED_LIST = "credList";								//$NON-NLS-1$
	private static final String TEST_TYPE_LIST = "testTypeList";					//$NON-NLS-1$
	private static final String SCAN_TYPE_LIST = "scanTypeList";					//$NON-NLS-1$
	private static final String TEST_OPTIMIZATION_LIST = "testOptimizationList";	//$NON-NLS-1$
	private static final String FAIL_BUILD_LIST = "failBuildList";					//$NON-NLS-1$
	private static final String STATIC_SCAN_SPEED_LIST = "staticScanSpeedList";		//$NON-NLS-1$

	private UIConfigSupport uiConfigSupport;
	private I18nBean i18nBean;
	
	public ScanTaskConfigurator(
			@ComponentImport I18nBeanFactory i18nBeanFactory) {
		
		this.uiConfigSupport = ComponentLocator.getComponent(UIConfigSupport.class);
		this.i18nBean = i18nBeanFactory.getI18nBean();
	}

	@Override
	public void populateContextForCreate(Map<String, Object> context) {
		context.put(TEST_TYPE_LIST, Utility.getTestTypes());
		context.put(SCAN_TYPE_LIST, Utility.getScanTypes());
		context.put(TEST_OPTIMIZATION_LIST, Utility.getTestOptimizations(i18nBean));
		context.put(FAIL_BUILD_LIST, Utility.getFailBuildTypes(i18nBean));
		context.put(STATIC_SCAN_SPEED_LIST, Utility.getStaticScanSpeedList(i18nBean));
		context.put(CFG_SEL_TEST_TYPE, SASTConstants.STATIC_ANALYZER); // Set Default Test Type to Static Analyzer
		context.put(CFG_STATIC_SCAN_SPEED, SCAN_SPEED_DEEP); // Set Default Scan Speed for Static Scan
		context.put(CFG_SEL_TEST_OPTIMIZE, OPTIMIZATION_FAST); // Set Default Test Optimization for Dynamic Scan
	}
	
	@Override
	public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
		context.put(TEST_TYPE_LIST, Utility.getTestTypes());
		context.put(SCAN_TYPE_LIST, Utility.getScanTypes());
		context.put(TEST_OPTIMIZATION_LIST, Utility.getTestOptimizations(i18nBean));
		context.put(FAIL_BUILD_LIST, Utility.getFailBuildTypes(i18nBean));
		context.put(STATIC_SCAN_SPEED_LIST, Utility.getStaticScanSpeedList(i18nBean));
		Map<String, String> config = taskDefinition.getConfiguration();
		context.put(CFG_API_KEY, config.get(CFG_API_KEY));
		context.put(CFG_API_SECRET, config.get(CFG_API_SECRET));
		context.put(CFG_SEL_TEST_TYPE, config.get(CFG_SEL_TEST_TYPE));
		context.put(CFG_APP_ID, config.get(CFG_APP_ID));
		context.put(CFG_SCAN_NAME, config.get(CFG_SCAN_NAME));
		context.put(CFG_EMAIL_NOTIFICATION, Boolean.valueOf(config.get(CFG_EMAIL_NOTIFICATION)));
		context.put(CFG_SUSPEND, Boolean.valueOf(config.get(CFG_SUSPEND)));
		context.put(CFG_MAX_TOTAL, config.get(CFG_MAX_TOTAL));
		context.put(CFG_MAX_HIGH, config.get(CFG_MAX_HIGH));
		context.put(CFG_MAX_MEDIUM, config.get(CFG_MAX_MEDIUM));
		context.put(CFG_MAX_LOW, config.get(CFG_MAX_LOW));
		context.put(CoreConstants.TARGET, config.get(CoreConstants.TARGET));
		context.put(CFG_SEL_SCAN_TYPE, config.get(CFG_SEL_SCAN_TYPE));
		String testOptimization = config.get(CFG_SEL_TEST_OPTIMIZE) == null ? OPTIMIZATION_FAST : config.get(CFG_SEL_TEST_OPTIMIZE); // Set Default Test Optimization
		context.put(CFG_SEL_TEST_OPTIMIZE, testOptimization);
		context.put(CFG_LOGIN_USER, config.get(CFG_LOGIN_USER));
		context.put(CFG_LOGIN_PASSWORD, config.get(CFG_LOGIN_PASSWORD));
		context.put(CFG_THIRD_CREDENTIAL, config.get(CFG_THIRD_CREDENTIAL));
		context.put(CFG_SEL_PRESENCE, config.get(CFG_SEL_PRESENCE));
		context.put(CFG_SCAN_FILE, config.get(CFG_SCAN_FILE));
		context.put(CFG_FAIL_BUILD_OPTION, config.get(CFG_FAIL_BUILD_OPTION));
		context.put(CFG_FAIL_BUILD, config.get(CFG_FAIL_BUILD));
		context.put(OPEN_SOURCE_ONLY, config.get(OPEN_SOURCE_ONLY));
		context.put(CFG_STATIC_SCAN_SPEED_CONF, config.get(CFG_STATIC_SCAN_SPEED_CONF));
		String staticScanSpeed = config.get(CFG_STATIC_SCAN_SPEED);
		staticScanSpeed = (staticScanSpeed == null || staticScanSpeed.isEmpty()) ? SCAN_SPEED_DEEP : staticScanSpeed; // Set Default Scan Speed
		context.put(CFG_STATIC_SCAN_SPEED, staticScanSpeed);
	}
	
	private void validateRequired(ActionParametersMap params, ErrorCollection errorCollection, String field) {
		String value = params.getString(field);
		if (value == null || value.trim().isEmpty())
			errorCollection.addError(field, i18nBean.getText("err." + field)); //$NON-NLS-1$
	}
	
	private void validateNumber(ActionParametersMap params, ErrorCollection errorCollection, String field) {
		String value = params.getString(field);
		if (!("".equals(value) || StringUtils.isNumeric(value)))		//$NON-NLS-1$
			errorCollection.addError(field, i18nBean.getText("err.nan"));	//$NON-NLS-1$
	}
	
	@Override
	public void validate(ActionParametersMap params, ErrorCollection errorCollection) {
		validateRequired(params, errorCollection, CFG_API_KEY);
		validateRequired(params, errorCollection, CFG_API_SECRET);
		validateRequired(params, errorCollection, CFG_APP_ID);
		validateRequired(params, errorCollection, CFG_SEL_TEST_TYPE);
		if (params.getString(CFG_SEL_TEST_TYPE) != null && DASTConstants.DYNAMIC_ANALYZER.equals(params.getString(CFG_SEL_TEST_TYPE))) {
			validateRequired(params, errorCollection, CoreConstants.TARGET);
		}
		validateNumber(params, errorCollection, CFG_MAX_TOTAL);
		validateNumber(params, errorCollection, CFG_MAX_HIGH);
		validateNumber(params, errorCollection, CFG_MAX_MEDIUM);
		validateNumber(params, errorCollection, CFG_MAX_LOW);
	}
	
	@Override
	public Map<String, String> generateTaskConfigMap(ActionParametersMap params, TaskDefinition previousTaskDefinition) {
		Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
		config.put(CFG_API_KEY, params.getString(CFG_API_KEY));
		config.put(CFG_API_SECRET, params.getString(CFG_API_SECRET));
		config.put(CFG_SEL_TEST_TYPE, params.getString(CFG_SEL_TEST_TYPE));
		config.put(CFG_APP_ID, params.getString(CFG_APP_ID));
		config.put(CFG_SCAN_NAME, params.getString(CFG_SCAN_NAME));
		config.put(CFG_EMAIL_NOTIFICATION, Boolean.toString(params.getBoolean(CFG_EMAIL_NOTIFICATION)));
		config.put(CFG_SUSPEND, Boolean.toString(params.getBoolean(CFG_SUSPEND)));
		config.put(CoreConstants.TARGET, params.getString(CoreConstants.TARGET));
		config.put(CFG_FAIL_BUILD_OPTION, params.getString(CFG_FAIL_BUILD_OPTION));
		if (params.getBoolean(CFG_FAIL_BUILD_OPTION)) {
			config.put(CFG_FAIL_BUILD, params.getString(CFG_FAIL_BUILD));
			if (FAIL_SEVERITY_LEVEL.equals(params.getString(CFG_FAIL_BUILD))) {
				config.put(CFG_MAX_TOTAL, params.getString(CFG_MAX_TOTAL));
				config.put(CFG_MAX_HIGH, params.getString(CFG_MAX_HIGH));
				config.put(CFG_MAX_MEDIUM, params.getString(CFG_MAX_MEDIUM));
				config.put(CFG_MAX_LOW, params.getString(CFG_MAX_LOW));
			}
		}
		if (DASTConstants.DYNAMIC_ANALYZER.equals(params.getString(CFG_SEL_TEST_TYPE))) {
			config.put(CFG_SEL_SCAN_TYPE, params.getString(CFG_SEL_SCAN_TYPE));
			config.put(CFG_SEL_TEST_OPTIMIZE, params.getString(CFG_SEL_TEST_OPTIMIZE));
			config.put(CFG_LOGIN_USER, params.getString(CFG_LOGIN_USER));
			config.put(CFG_LOGIN_PASSWORD, params.getString(CFG_LOGIN_PASSWORD));
			config.put(CFG_THIRD_CREDENTIAL, params.getString(CFG_THIRD_CREDENTIAL));
			config.put(CFG_SEL_PRESENCE, params.getString(CFG_SEL_PRESENCE));
			config.put(CFG_SCAN_FILE, params.getString(CFG_SCAN_FILE));
		}
		if (SASTConstants.STATIC_ANALYZER.equals(params.getString(CFG_SEL_TEST_TYPE))) {
			config.put(OPEN_SOURCE_ONLY, params.getString(OPEN_SOURCE_ONLY));
			config.put(CFG_STATIC_SCAN_SPEED_CONF, params.getString(CFG_STATIC_SCAN_SPEED_CONF));
			if (params.getBoolean(CFG_STATIC_SCAN_SPEED_CONF)) {
				config.put(CFG_STATIC_SCAN_SPEED, params.getString(CFG_STATIC_SCAN_SPEED));
			}
		}
		return config;
	}
	
	@Override
	public Set<Requirement> calculateRequirements(TaskDefinition taskDefinition) {
		return new HashSet<Requirement>();
	}
}
