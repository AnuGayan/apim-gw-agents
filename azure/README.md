# Azure API Management Gateway Deployer for WSO2 API Manager

This document describes the Azure API Management Gateway Deployer, a component for WSO2 API Manager that enables publishing APIs to Microsoft Azure API Management service.

## Overview

The Azure Gateway Deployer allows WSO2 API Manager to act as a control plane for APIs that are deployed to Azure API Management. This enables a hybrid API management scenario where API design, lifecycle management, and governance can be centralized in WSO2 API Manager, while the API runtime and traffic management are handled by Azure API Management.

## Features (Initial Version)

*   **API Deployment:** Deploy REST APIs created in WSO2 API Manager to a specified Azure API Management service.
*   **API Undeployment:** Remove APIs deployed to Azure API Management.
*   **API Updates:** Re-deploy APIs to update existing instances in Azure API Management.
*   **Basic Validation:** Perform rudimentary validation of API definitions before deployment.
*   **Dynamic Environment Configuration:** Configure Azure service details (Resource Group, Service Name, Subscription, Credentials) within WSO2 API Manager's environment settings.

## Prerequisites

1.  **Azure Subscription:** An active Microsoft Azure subscription.
2.  **Azure API Management Service:** A provisioned instance of Azure API Management.
3.  **Azure Service Principal:**
    *   An Azure Active Directory (Azure AD) Application Registration.
    *   A Service Principal associated with the App Registration.
    *   The Service Principal must have at least "Contributor" role permissions on the Azure API Management service instance and its Resource Group.
    *   You will need the following details from the Service Principal:
        *   Tenant ID (Directory ID)
        *   Client ID (Application ID)
        *   Client Secret
4.  **WSO2 API Manager:** A running instance of WSO2 API Manager (version compatible with `org.wso2.carbon.apimgt.api` version used in `pom.xml`, e.g., 9.31.x corresponding to APIM 4.x.x).
5.  **Network Connectivity:** WSO2 API Manager instance must be able to connect to Azure management endpoints (`management.azure.com`).

## Installation & Configuration

1.  **Build the Component:**
    *   Clone the repository (if applicable).
    *   Navigate to the `extensions/azure/` directory (or the root of this specific Azure component project).
    *   Build the Maven project:
        ```bash
        mvn clean install
        ```
    *   This will produce an OSGi bundle, typically found at `extensions/azure/components/azure.gw.manager/target/azure.gw.manager-1.0.0-SNAPSHOT.jar`.

2.  **Deploy the Bundle:**
    *   Copy the compiled JAR file (`azure.gw.manager-1.0.0-SNAPSHOT.jar`) to your WSO2 API Manager instance's dropins directory: `<APIM_HOME>/repository/components/dropins/`.
    *   Restart WSO2 API Manager if it was already running.

3.  **Configure Azure Environment in WSO2 API Manager:**
    *   Log in to the WSO2 Admin Portal (`https://<APIM_HOST>:<PORT>/admin`).
    *   Navigate to **Settings > Gateway Environments**.
    *   Click **Add New Environment**.
    *   Fill in the environment details:
        *   **Name:** A descriptive name (e.g., "Azure Production", "Azure Dev").
        *   **Type:** Select "Azure" from the dropdown.
        *   **Display Name:** A user-friendly name.
        *   **Description:** Optional description.
        *   **Gateway Endpoint(s):** While Azure APIM has its own endpoints, you might put a placeholder or the APIM developer portal URL here. The actual API URLs will be derived.
    *   Under **Additional Properties**, add the following:
        *   `resource_group`: Your Azure Resource Group name where the APIM service resides.
        *   `service_name`: The name of your Azure API Management service instance.
        *   `subscription_id`: Your Azure Subscription ID.
        *   `tenant_id`: The Tenant ID of your Azure AD Service Principal.
        *   `client_id`: The Client ID of your Azure AD Service Principal.
        *   `client_secret`: The Client Secret of your Azure AD Service Principal.
    *   Save the environment.

## Usage

1.  **Design and Create APIs:** Use the WSO2 Publisher Portal (`httpshttps://<APIM_HOST>:<PORT>/publisher`) to design and create your APIs as usual.
2.  **Deploy to Azure:**
    *   Navigate to the "Deployments" section of your API in the Publisher.
    *   Select the newly configured Azure environment.
    *   Deploy the API revision.
3.  **Verify in Azure:**
    *   Log in to the Azure Portal.
    *   Navigate to your API Management service.
    *   Under the "APIs" blade, you should see the API deployed from WSO2. The API name in Azure will typically be a combination of the WSO2 API name, version, and a unique identifier.
4.  **Undeploy from Azure:**
    *   In the WSO2 Publisher, remove the API deployment from the Azure environment.
    *   This will trigger a delete operation in the Azure API Management service for that API.

## Current Limitations & Future Work

*   **Basic API Structure:** The initial version primarily deploys the basic API contract (name, path, backend URL). Detailed mapping of WSO2 URI templates to Azure Operations, including request/response schemas, parameters, and headers, is a work in progress.
*   **Policy Mapping:** Advanced WSO2 policy mediation is not yet translated into equivalent Azure API Management policies.
*   **Product Association:** APIs are deployed but not automatically associated with specific Azure APIM Products. This needs to be managed manually in Azure or enhanced in the deployer.
*   **Security Schemes:** Limited mapping of WSO2 security schemes (e.g., OAuth2, API Key) to Azure APIM security settings.
*   **Revisions and Versioning:** Alignment between WSO2 API versions/revisions and Azure APIM versions/revisions needs refinement.
*   **Error Reporting:** Enhanced error reporting and feedback from Azure to WSO2.

## Troubleshooting

*   **Logs:** Check WSO2 API Manager logs (`wso2carbon.log`) for messages from `org.wso2.azure.client` classes.
*   **Azure Activity Log:** Review the Activity Log for your Azure API Management service in the Azure Portal for details on operations performed by the deployer.
*   **Permissions:** Ensure the Service Principal has the correct permissions in Azure.
*   **Configuration:** Double-check all environment configuration properties in WSO2 Admin Portal.

---

This README provides a starting point. It should be updated as the component evolves.
