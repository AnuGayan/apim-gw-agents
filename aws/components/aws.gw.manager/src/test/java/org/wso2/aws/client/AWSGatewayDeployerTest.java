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

package org.wso2.aws.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wso2.aws.client.util.AWSAPIUtil;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.RestApi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AWSGatewayDeployerTest {

    @Mock
    private ApiGatewayClient mockApiGatewayClient;

    @Mock
    private Environment mockEnvironment;

    private AWSGatewayDeployer awsGatewayDeployer;

    @BeforeEach
    void setUp() throws APIManagementException {
        // Initialize AWSGatewayDeployer and inject the mock ApiGatewayClient via the Environment
        awsGatewayDeployer = new AWSGatewayDeployer();
        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put(AWSConstants.AWS_ENVIRONMENT_REGION, "us-east-1");
        additionalProperties.put(AWSConstants.AWS_API_STAGE, "dev");
        additionalProperties.put(AWSConstants.AWS_ENVIRONMENT_ACCESS_KEY, "dummyAccessKey");
        additionalProperties.put(AWSConstants.AWS_ENVIRONMENT_SECRET_KEY, "dummySecretKey");

        when(mockEnvironment.getAdditionalProperties()).thenReturn(additionalProperties);
        // This part is a bit tricky as the ApiGatewayClient is created inside init.
        // For a real test, we might need to refactor AWSGatewayDeployer to allow client injection
        // or use PowerMockito to mock the client construction if it were a more complex scenario.
        // However, for this specific test of discoverAPIs, the internal client's direct calls are not an issue
        // as we are mocking the static AWSAPIUtil.getRestApis method.
        // For the sake of this example, we'll proceed assuming init works or is not strictly needed for discoverAPIs logic itself
        // if AWSAPIUtil is correctly mocked.

        // A simplified init process for the test, focusing on what discoverAPIs might need.
        // The actual apiGatewayClient inside awsGatewayDeployer will be the one created by its init method.
        // We will mock the static AWSAPIUtil.getRestApis to control its behavior directly.
        awsGatewayDeployer.init(mockEnvironment); // This will create the real client internally.
    }

    @Test
    void testDiscoverAPIs() throws APIManagementException {
        // Prepare mock RestApi objects
        RestApi api1 = RestApi.builder().id("id1").name("API 1").build();
        RestApi api2 = RestApi.builder().id("id2").name("API 2").build();
        List<RestApi> mockRestApis = Arrays.asList(api1, api2);

        List<String> expectedApiNames = Arrays.asList("API 1", "API 2");

        // Mock the static method AWSAPIUtil.getRestApis
        // The ApiGatewayClient passed to AWSAPIUtil.getRestApis will be the one from awsGatewayDeployer.apiGatewayClient
        try (MockedStatic<AWSAPIUtil> mockedStaticAPIUtil = Mockito.mockStatic(AWSAPIUtil.class)) {
            mockedStaticAPIUtil.when(() -> AWSAPIUtil.getRestApis(Mockito.any(ApiGatewayClient.class)))
                               .thenReturn(mockRestApis);

            // Call the method to be tested
            List<String> actualApiNames = awsGatewayDeployer.discoverAPIs();

            // Verify the results
            assertNotNull(actualApiNames, "Returned list of API names should not be null");
            assertEquals(expectedApiNames.size(), actualApiNames.size(), "Number of API names should match");
            assertEquals(expectedApiNames.get(0), actualApiNames.get(0), "API 1 name should match");
            assertEquals(expectedApiNames.get(1), actualApiNames.get(1), "API 2 name should match");

            // Verify that AWSAPIUtil.getRestApis was called
            // The argument to getRestApis should be the client instance held by awsGatewayDeployer
            mockedStaticAPIUtil.verify(() -> AWSAPIUtil.getRestApis(Mockito.any(ApiGatewayClient.class)));
        }
    }
}
