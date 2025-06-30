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

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.apimanagement.ApiManagementManager;
import com.azure.resourcemanager.apimanagement.models.ApiContract;
import com.azure.resourcemanager.apimanagement.models.ApiCreateOrUpdateParameter;
import com.azure.resourcemanager.apimanagement.models.ApiType;
import com.azure.resourcemanager.apimanagement.models.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayAPIValidationResult;
import org.wso2.carbon.apimgt.api.model.GatewayDeployer;
import org.wso2.carbon.apimgt.api.model.URITemplate;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class controls the API artifact deployments on the Azure API Management Gateway
 */
public class AzureGatewayDeployer implements GatewayDeployer {
    private static final Log log = LogFactory.getLog(AzureGatewayDeployer.class);
    private ApiManagementManager apiManagementManager;
    private String resourceGroupName;
    private String serviceName;
    private String subscriptionId;


    @Override
    public void init(Environment environment) throws APIManagementException {
        try {
            this.resourceGroupName = environment.getAdditionalProperties().get(AzureConstants.AZURE_ENVIRONMENT_RESOURCE_GROUP);
            this.serviceName = environment.getAdditionalProperties().get(AzureConstants.AZURE_ENVIRONMENT_SERVICE_NAME);
            this.subscriptionId = environment.getAdditionalProperties().get(AzureConstants.AZURE_SUBSCRIPTION_ID);
            String tenantId = environment.getAdditionalProperties().get(AzureConstants.AZURE_TENANT_ID);
            String clientId = environment.getAdditionalProperties().get(AzureConstants.AZURE_CLIENT_ID);
            String clientSecret = environment.getAdditionalProperties().get(AzureConstants.AZURE_CLIENT_SECRET);

            if (StringUtils.isAnyBlank(resourceGroupName, serviceName, subscriptionId, tenantId, clientId, clientSecret)) {
                throw new APIManagementException("Required Azure credentials are not provided in the environment configuration.");
            }

            TokenCredential credential = new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .tenantId(tenantId)
                    .build();

            AzureProfile profile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);

            this.apiManagementManager = ApiManagementManager
                    .authenticate(credential, profile);

            log.info("Azure API Management Deployer initialized successfully for service: " + serviceName);

        } catch (Exception e) {
            throw new APIManagementException("Error occurred while initializing Azure Gateway Deployer", e);
        }
    }

    @Override
    public String getType() {
        return AzureConstants.AZURE_TYPE;
    }

    @Override
    public String deploy(API api, String externalReference) throws APIManagementException {
        String apiId = StringUtils.isBlank(externalReference) ? generateAzureApiId(api.getId().getApiName(), api.getId().getVersion()) : externalReference;

        try {
            ApiCreateOrUpdateParameter apiCreateOrUpdateParameter = new ApiCreateOrUpdateParameter()
                    .withDisplayName(api.getId().getApiName() + "-" + api.getId().getVersion())
                    .withPath(api.getContext()) // Assuming API context is the base path
                    .withProtocols(Arrays.asList(Protocol.HTTPS)) // Defaulting to HTTPS
                    .withDescription(api.getDescription())
                    .withServiceUrl(api.getEndpointConfig()) // This needs careful mapping
                    .withApiType(ApiType.HTTP); // Defaulting to HTTP, can be extended

            // TODO: Map URI templates to Azure API Management Operations
            // TODO: Handle policies, products, revisions, etc.

            ApiContract createdApi = apiManagementManager.apis().define(apiId)
                    .withExistingService(resourceGroupName, serviceName)
                    .withProperties(apiCreateOrUpdateParameter)
                    .create();

            log.info("API deployed/updated in Azure: " + createdApi.id());
            return createdApi.name(); // Return the API name (which is its ID in Azure APIM)

        } catch (Exception e) {
            throw new APIManagementException("Error deploying API to Azure API Management: " + api.getId().getApiName(), e);
        }
    }

    private String generateAzureApiId(String apiName, String version) {
        // Azure API ID needs to be unique, typically can be a sanitized name + version or a GUID
        // For simplicity, let's use a sanitized name and version.
        // Alternatively, could be `api.getId().toUUID()` if that's preferred and stable.
        String sanitizedName = apiName.replaceAll("[^a-zA-Z0-9_.-]", "-");
        String sanitizedVersion = version.replaceAll("[^a-zA-Z0-9_.-]", "-");
        return sanitizedName + "_" + sanitizedVersion + "_" + UUID.randomUUID().toString().substring(0,8) ;
    }


    @Override
    public boolean undeploy(String externalReference) throws APIManagementException {
        if (StringUtils.isBlank(externalReference)) {
            log.warn("External reference (Azure API ID) is missing. Cannot undeploy.");
            return false;
        }
        try {
            // Check if API exists before attempting to delete
            ApiContract apiContract = apiManagementManager.apis().get(resourceGroupName, serviceName, externalReference);
            if (apiContract == null) {
                log.warn("API with ID '" + externalReference + "' not found in Azure. Assuming already undeployed.");
                return true; // Or false depending on desired strictness
            }

            apiManagementManager.apis().delete(resourceGroupName, serviceName, externalReference, "*"); // "*" for If-Match etag
            log.info("API undeployed from Azure: " + externalReference);
            return true;
        } catch (Exception e) {
            // Handle cases where API is not found (which might be okay for undeploy)
            // Or other Azure specific exceptions
            if (e.getMessage() != null && e.getMessage().contains("ResourceNotFound")) {
                log.warn("API with ID '" + externalReference + "' not found during undeploy operation in Azure. Assuming already undeployed.", e);
                return true; // Successfully undeployed as it's not there.
            }
            throw new APIManagementException("Error undeploying API from Azure API Management: " + externalReference, e);
        }
    }

    @Override
    public GatewayAPIValidationResult validateApi(API api) throws APIManagementException {
        List<String> errorList = new ArrayList<>();
        // Example Validation: Check if context is provided
        if (StringUtils.isBlank(api.getContext())) {
            errorList.add("API context (base path) is missing or empty.");
        }
        // Example: Azure path segments cannot contain certain characters like '//', '.', etc.
        // This is a simplistic check. Real validation would involve checking Azure naming constraints.
        if (api.getContext() != null && (api.getContext().contains("//") || api.getContext().contains("."))) {
            errorList.add("API context contains invalid characters for Azure path segments (e.g., '//', '.').");
        }

        // Endpoint validation (basic)
        if (StringUtils.isBlank(api.getEndpointConfig())) {
             errorList.add("API endpoint configuration (service URL) is missing.");
        }
        // Further validation for URI templates if needed

        GatewayAPIValidationResult result = new GatewayAPIValidationResult();
        result.setValid(errorList.isEmpty());
        result.setErrors(errorList);
        return result;
    }

    @Override
    public String getAPIExecutionURL(String externalReference) throws APIManagementException {
        if (StringUtils.isAnyBlank(serviceName, externalReference)) {
            throw new APIManagementException("Service name or external reference (API ID) is null, cannot generate execution URL.");
        }
        // The base URL for the APIM service. The actual API path is usually determined by the API's configuration.
        // Example: https://{serviceName}.azure-api.net/{apiPath}
        // We need to fetch the API path from Azure or construct it if we know the convention.
        // For now, returning the service URL. The API specific path might need to be appended by the caller
        // or this method needs to fetch the API's path property.

        try {
            ApiContract apiContract = apiManagementManager.apis().get(resourceGroupName, serviceName, externalReference);
            if (apiContract == null) {
                throw new APIManagementException("API with ID '" + externalReference + "' not found in Azure.");
            }
            // The full invoke URL is typically: gateway-url/path-prefix/operation-path
            // The gateway URL is serviceName.azure-api.net
            // The path-prefix is apiContract.path()
            // Operation path is specific to the operation.
            // This method should return the base URL for the API.
            String baseUrl = "https://" + serviceName + ".azure-api.net";
            if (apiContract.path() != null && !apiContract.path().isEmpty()) {
                return baseUrl + "/" + apiContract.path().replaceAll("^/+", ""); // Ensure single slash
            }
            return baseUrl;

        } catch (Exception e) {
             throw new APIManagementException("Could not retrieve API details to form execution URL for API ID: " + externalReference, e);
        }
    }

    @Override
    public void transformAPI(API api) throws APIManagementException {
        // Example Transformation: Ensure context starts with a slash if not empty
        if (StringUtils.isNotBlank(api.getContext()) && !api.getContext().startsWith("/")) {
            api.setContext("/" + api.getContext());
        }

        // Azure paths are relative to the API's base path (context).
        // Unlike AWS /* mapping, Azure operations are defined with specific paths.
        // We might need to ensure resource paths are well-formed for Azure.
        for (URITemplate resource : api.getUriTemplates()) {
            String template = resource.getUriTemplate();
            // Ensure paths don't have problematic wildcards if Azure doesn't support them directly in the same way.
            // Azure uses path templates like /users/{userId}, not /users/* in the same way for matching.
            // This transformation might be more about ensuring clean paths.
            if (template.endsWith("/*")) {
                // Decide on a convention. Maybe remove it, or replace with a parameter if appropriate.
                // For now, let's just log or remove it if it's problematic.
                // resource.setUriTemplate(template.substring(0, template.length() - 2)); // Removes /*
                log.debug("Transforming URI template: " + template + ". Check Azure compatibility for wildcards.");
            }
            // Ensure resource paths are relative and don't start with the API context again.
            if (api.getContext() != null && template.startsWith(api.getContext())) {
                resource.setUriTemplate(template.substring(api.getContext().length()));
            }
            // Ensure template starts with / if not empty
             if (StringUtils.isNotBlank(resource.getUriTemplate()) && !resource.getUriTemplate().startsWith("/")) {
                resource.setUriTemplate("/" + resource.getUriTemplate());
            }
        }
        log.info("API transformation step completed for API: " + api.getId().getApiName());
    }
}
