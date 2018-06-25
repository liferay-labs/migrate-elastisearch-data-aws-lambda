/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.osb.pulpo.lambda.handler.elasticsearch;

import com.amazonaws.AmazonWebServiceResponse;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.util.StringUtils;

import com.liferay.osb.pulpo.lambda.handler.MigrationRequest;
import com.liferay.osb.pulpo.lambda.handler.http.SimpleHttpErrorResponseHandler;
import com.liferay.osb.pulpo.lambda.handler.http.StringResponseHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.net.URI;

import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author David Arques
 */
public class ElasticSearchAWSUtil {

	/**
	 * Lists the snapshots made in the given AWS Elasticsearch repository.
	 *
	 * @param request input request
	 * @param logger lambda logger
	 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-snapshots.html'>cat-snapshots</a>
	 * @return String containing the result of the request
	 */
	public static String migrateEsData(
		MigrationRequest request, LambdaLogger logger) {

		logger.log("Migrating ES Data with  " + request);

		_validateInputRequest(request);

		String backup = _backupIndices(request, logger);

		_checkBackupExistence(request, backup, logger);

		_deleteAllIndices(request, logger);

		return _restoreBackup(request, backup, logger);
	}

	private static String _backupIndices(
		MigrationRequest migration, LambdaLogger lambdaLogger) {

		_createBackupRepo(
			migration.getBucket(), migration.getRole(),
			migration.getOriginHost());

		return _createBackup(migration, lambdaLogger);
	}

	private static void _checkBackupExistence(
		MigrationRequest migrationRequest, String backup,
		LambdaLogger lambdaLogger) {

		Request<Void> awsRequest = _createAwsRequest(
			migrationRequest.getOriginHost(),
			String.format(
				"/_snapshot/%s/%s", migrationRequest.getBucket(), backup),
			Collections.emptyMap(), HttpMethodName.GET, null);

		lambdaLogger.log(_EXECUTING_AWS_REQUEST + awsRequest);

		Response<AmazonWebServiceResponse<String>> awsResponse =
			_executeAwsRequest(awsRequest);

		lambdaLogger.log(
			_AMAZON_WEB_SERVICE_RESPONSE +
				awsResponse.getAwsResponse().getResult());

		if (awsResponse.getHttpResponse().getStatusCode() == 404) {
			throw new IllegalArgumentException(
				"Bucket " + backup + " not found");
		}
	}

	private static Request<Void> _createAwsRequest(
		String host, String path, Map<String, List<String>> params,
		HttpMethodName httpMethodName, String content) {

		Request<Void> request = new DefaultRequest<>("es");

		request.setHttpMethod(httpMethodName);

		request.setEndpoint(URI.create(host));

		request.setResourcePath(path);

		if (!params.isEmpty()) {
			request.setParameters(params);
		}

		if (content != null) {
			InputStream contentInputStream = new ByteArrayInputStream(
				content.getBytes());

			request.setContent(contentInputStream);
		}

		AWS4Signer signer = new AWS4Signer();

		signer.setServiceName(request.getServiceName());
		signer.setRegionName(_REGION);
		signer.sign(
			request, new DefaultAWSCredentialsProviderChain().getCredentials());

		return request;
	}

	private static String _createBackup(
		MigrationRequest migrationRequest, LambdaLogger lambdaLogger) {

		String backupName =
			migrationRequest.getOriginIndicesPrefix() +
				_format.format(new Date());

		lambdaLogger.log("Creating Backup; " + backupName);

		Request<Void> backupRequest = _createAwsRequest(
			migrationRequest.getOriginHost(),
			"/_snapshot/" + migrationRequest.getBucket() + "/" + backupName,
			Collections.emptyMap(), HttpMethodName.PUT,
			"{\"ignore_unavailable\": true, \"include_global_state\": false}");

		_executeAwsRequest(backupRequest);

		return backupName;
	}

	private static void _createBackupRepo(
		String bucket, String role, String host) {

		String content =
			"{\"type\": \"s3\",\"settings\": { \"bucket\": \"" + bucket +
				"\",\"endpoint\": \"s3.amazonaws.com\",\"role_arn\": \"" +
					role + "\"}}";

		Request<Void> setupBucket = _createAwsRequest(
			host, "/_snapshot/" + bucket, Collections.emptyMap(),
			HttpMethodName.PUT, content);

		_executeAwsRequest(setupBucket);
	}

	private static void _deleteAllIndices(
		MigrationRequest migrationRequest, LambdaLogger logger) {

		Request<Void> awsRequest = _createAwsRequest(
			migrationRequest.getDestinationHost(), "_all",
			Collections.emptyMap(), HttpMethodName.DELETE, null);

		logger.log(_EXECUTING_AWS_REQUEST + awsRequest);

		Response<AmazonWebServiceResponse<String>> awsResponse =
			_executeAwsRequest(awsRequest);

		logger.log(
			_AMAZON_WEB_SERVICE_RESPONSE +
				awsResponse.getAwsResponse().getResult());
	}

	private static Response<AmazonWebServiceResponse<String>>
		_executeAwsRequest(Request<Void> request) {

		ClientConfiguration config = new ClientConfiguration();

		AmazonHttpClient.RequestExecutionBuilder builder = new AmazonHttpClient(
			config).requestExecutionBuilder();

		return builder.executionContext(
			new ExecutionContext(true)
		).request(
			request
		).errorResponseHandler(
			new SimpleHttpErrorResponseHandler()
		).execute(
			new StringResponseHandler()
		);
	}

	private static String _restoreBackup(
		MigrationRequest request, String backup, LambdaLogger logger) {

		_createBackupRepo(
			request.getBucket(), request.getRole(),
			request.getDestinationHost());

		Request<Void> awsRequest = _createAwsRequest(
			request.getDestinationHost(),
			String.format(
				"/_snapshot/%s/%s/_restore", request.getBucket(), backup),
			Collections.emptyMap(), HttpMethodName.POST,
			_restoreRequestContent(request));

		logger.log(_EXECUTING_AWS_REQUEST + awsRequest);

		Response<AmazonWebServiceResponse<String>> awsResponse =
			_executeAwsRequest(awsRequest);

		logger.log(
			_AMAZON_WEB_SERVICE_RESPONSE +
				awsResponse.getAwsResponse().getResult());

		return awsResponse.getAwsResponse().getResult();
	}

	private static String _restoreRequestContent(MigrationRequest request) {
		String prefixIn = request.getOriginIndicesPrefix();
		String prefixOut = request.getDestinationIndicesPrefix();

		String content =
			"{\n\"indices\": \"" + prefixIn + "*\",\n\"rename_pattern\": \"" +
				prefixIn + "(.+)\",\n\"rename_replacement\": \"" + prefixOut +
					"$1\",\n\"ignore_unavailable\": true,\n" +
						"\"include_global_state\": true\n" + "}";

		return content;
	}

	private static void _validateInputRequest(
		MigrationRequest migrationRequest) {

		if (migrationRequest == null) {
			throw new IllegalArgumentException(
				"MigrationRequest must not be null");
		}

		if (StringUtils.isNullOrEmpty(migrationRequest.getOriginHost())) {
			throw new IllegalArgumentException("Origin Host must not be empty");
		}

		if (StringUtils.isNullOrEmpty(migrationRequest.getDestinationHost())) {
			throw new IllegalArgumentException(
				"Destination Host must not be empty");
		}

		if (StringUtils.isNullOrEmpty(migrationRequest.getBucket())) {
			throw new IllegalArgumentException("Bucket name must not be empty");
		}

		if (StringUtils.isNullOrEmpty(
				migrationRequest.getOriginIndicesPrefix())) {

			throw new IllegalArgumentException("Backup id must not be empty");
		}
	}

	private static final String _AMAZON_WEB_SERVICE_RESPONSE = "\nRESPONSE: ";

	private static final String _EXECUTING_AWS_REQUEST = "\nEXECUTE: ";

	private static final String _REGION = System.getenv(
		SDKGlobalConfiguration.AWS_REGION_ENV_VAR);

	private static final SimpleDateFormat _format = new SimpleDateFormat(
		"yyyyMMddHHmmss");

}