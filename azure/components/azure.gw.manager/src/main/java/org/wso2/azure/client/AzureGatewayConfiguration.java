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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.GatewayPortalConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the configurations related to Azure API Management Gateway
 */
@Component(
        name = "azure.external.gateway.configuration.component",
        immediate = true,
        service = GatewayAgentConfiguration.class
)
public class AzureGatewayConfiguration implements GatewayAgentConfiguration {
    private static final Log log = LogFactory.getLog(AzureGatewayConfiguration.class); // Changed from AWSAPIUtil

    @Override
    public String getImplementation() {
        return AzureGatewayDeployer.class.getName();
    }

    @Override
    public List<ConfigurationDto> getConnectionConfigurations() {
        List<ConfigurationDto> configurationDtoList = new ArrayList<>();
        configurationDtoList.add(new ConfigurationDto(AzureConstants.AZURE_ENVIRONMENT_RESOURCE_GROUP, "Resource Group",
                "input", "Azure Resource Group Name", "", true, false, Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto(AzureConstants.AZURE_ENVIRONMENT_SERVICE_NAME, "API Management Service Name",
                "input", "Azure API Management Service Name", "", true, false, Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto(AzureConstants.AZURE_SUBSCRIPTION_ID, "Subscription ID",
                "input", "Azure Subscription ID", "", true, false, Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto(AzureConstants.AZURE_TENANT_ID, "Tenant ID",
                "input", "Azure Tenant ID (Directory ID)", "", true, true, Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto(AzureConstants.AZURE_CLIENT_ID, "Client ID",
                "input", "Azure App Registration Client ID", "", true, true, Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto(AzureConstants.AZURE_CLIENT_SECRET, "Client Secret",
                "input", "Azure App Registration Client Secret", "", true, true, Collections.emptyList(), false));
        return configurationDtoList;
    }

    @Override
    public String getType() {
        return AzureConstants.AZURE_TYPE;
    }

    @Override
    public GatewayPortalConfiguration getGatewayFeatureCatalog() throws APIManagementException {
        try (InputStream inputStream = AzureGatewayConfiguration.class.getClassLoader()
                .getResourceAsStream("GatewayFeatureCatalog.json")) { // This file will be created in a later step

            if (inputStream == null) {
                // Fallback or default if JSON not found, can be improved
                log.warn("GatewayFeatureCatalog.json not found for Azure. Returning empty configuration.");
                GatewayPortalConfiguration defaultConfig = new GatewayPortalConfiguration();
                defaultConfig.setGatewayType(AzureConstants.AZURE_TYPE);
                defaultConfig.setSupportedAPITypes(Collections.singletonList("HTTP")); // Example
                defaultConfig.setSupportedFeatures(new JsonObject()); // Empty features
                return defaultConfig;
                // throw new APIManagementException("Gateway Feature Catalog JSON not found for Azure");
            }

            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            JsonObject gatewayObject = jsonObject.getAsJsonObject(AzureConstants.AZURE_TYPE);
            if (gatewayObject == null) {
                 log.warn("Azure type not found in GatewayFeatureCatalog.json. Returning empty configuration.");
                GatewayPortalConfiguration defaultConfig = new GatewayPortalConfiguration();
                defaultConfig.setGatewayType(AzureConstants.AZURE_TYPE);
                defaultConfig.setSupportedAPITypes(Collections.singletonList("HTTP")); // Example
                defaultConfig.setSupportedFeatures(new JsonObject()); // Empty features
                return defaultConfig;
                // throw new APIManagementException("Azure type not found in Gateway Feature Catalog JSON");
            }


            List<String> apiTypes = gson.fromJson(gatewayObject.get("apiTypes"),
                    new TypeToken<List<String>>() {}.getType());
            JsonObject gatewayFeatures = gatewayObject.get("gatewayFeatures").getAsJsonObject();

            GatewayPortalConfiguration config = new GatewayPortalConfiguration();
            config.setGatewayType(AzureConstants.AZURE_TYPE);
            config.setSupportedAPITypes(apiTypes);
            config.setSupportedFeatures(gatewayFeatures);

            return config;
        } catch (Exception e) {
            throw new APIManagementException("Error occurred while reading Azure Gateway Feature Catalog JSON", e);
        }
    }

    @Override
    public String getDefaultHostnameTemplate() {
        // e.g. {serviceName}.azure-api.net
        // The actual API path is usually appended later based on API definition
        return AzureConstants.AZURE_APIM_SERVICE_URL_TEMPLATE;
    }
}
