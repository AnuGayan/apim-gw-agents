/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.azure.client;

/**
 * This class contains the constants used in Azure client.
 */
public class AzureConstants {
    public static final String AZURE_TYPE = "Azure";

    // Environment related constants
    public static final String AZURE_ENVIRONMENT_RESOURCE_GROUP = "resource_group";
    public static final String AZURE_ENVIRONMENT_SERVICE_NAME = "service_name";
    public static final String AZURE_SUBSCRIPTION_ID = "subscription_id";
    public static final String AZURE_TENANT_ID = "tenant_id";
    public static final String AZURE_CLIENT_ID = "client_id";
    public static final String AZURE_CLIENT_SECRET = "client_secret";
    public static final String AZURE_API_VERSION = "2021-08-01"; // Example API version, might need adjustment

    // API Management specific
    public static final String AZURE_API_ID_PATTERN = "Id=([a-zA-Z0-9-]+)"; // Example, adjust if needed
    // {serviceName}.azure-api.net/{apiPath} - The apiPath is usually part of the API definition itself.
    // The base URL for the APIM service is typically {serviceName}.azure-api.net
    public static final String AZURE_APIM_SERVICE_URL_TEMPLATE = "{serviceName}.azure-api.net";


    // Default product name if applicable (Azure uses Products to manage API access and visibility)
    // It's common to associate APIs with a product (e.g., "Starter", "Unlimited")
    public static final String AZURE_DEFAULT_PRODUCT_ID = "starter"; // Example product ID

}
