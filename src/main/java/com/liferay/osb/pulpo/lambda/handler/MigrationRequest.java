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

package com.liferay.osb.pulpo.lambda.handler;

/**
 * @author David Arques
 */
public class MigrationRequest {

	public String getBucket() {
		return _bucket;
	}

	public String getDestinationHost() {
		return _destinationHost;
	}

	public String getDestinationIndicesPrefix() {
		return _destinationIndicesPrefix;
	}

	public String getOriginHost() {
		return _originHost;
	}

	public String getOriginIndicesPrefix() {
		return _originIndicesPrefix;
	}

	public String getRole() {
		return _role;
	}

	public void setBucket(String bucket) {
		_bucket = bucket;
	}

	public void setDestinationHost(String destinationHost) {
		_destinationHost = destinationHost;
	}

	public void setDestinationIndicesPrefix(String destinationIndicesPrefix) {
		_destinationIndicesPrefix = destinationIndicesPrefix;
	}

	public void setOriginHost(String originHost) {
		_originHost = originHost;
	}

	public void setOriginIndicesPrefix(String originIndicesPrefix) {
		_originIndicesPrefix = originIndicesPrefix;
	}

	public void setRole(String role) {
		_role = role;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(14);

		sb.append("MigrationRequest{");
		sb.append("_bucket='");
		sb.append(_bucket);
		sb.append("', _destinationHost='");
		sb.append(_destinationHost);
		sb.append("', _originHost='");
		sb.append(_originHost);
		sb.append("', _role='");
		sb.append(_role);
		sb.append("', _originIndicesPrefix='");
		sb.append(_originIndicesPrefix);
		sb.append("', _destinationIndicesPrefix='");
		sb.append(_destinationIndicesPrefix);
		sb.append("}");

		return sb.toString();
	}

	private String _bucket;
	private String _destinationHost;
	private String _destinationIndicesPrefix;
	private String _originHost;
	private String _originIndicesPrefix;
	private String _role;

}