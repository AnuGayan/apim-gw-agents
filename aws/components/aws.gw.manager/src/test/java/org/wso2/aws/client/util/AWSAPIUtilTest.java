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

package org.wso2.aws.client.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.GetRestApisRequest;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.RestApi;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AWSAPIUtilTest {

    @Mock
    private ApiGatewayClient mockApiGatewayClient;

    @Test
    void testGetRestApis() {
        // Prepare mock RestApi objects
        RestApi api1 = RestApi.builder().id("id1").name("API 1").build();
        RestApi api2 = RestApi.builder().id("id2").name("API 2").build();
        List<RestApi> expectedApis = Arrays.asList(api1, api2);

        // Mock the GetRestApisResponse
        GetRestApisResponse mockResponse = GetRestApisResponse.builder().items(expectedApis).build();

        // Mock the behavior of the ApiGatewayClient
        when(mockApiGatewayClient.getRestApis(any(GetRestApisRequest.class))).thenReturn(mockResponse);

        // Call the method to be tested
        List<RestApi> actualApis = AWSAPIUtil.getRestApis(mockApiGatewayClient);

        // Verify the results
        assertEquals(expectedApis.size(), actualApis.size(), "Number of APIs should match");
        assertEquals(expectedApis.get(0).name(), actualApis.get(0).name(), "API 1 name should match");
        assertEquals(expectedApis.get(1).name(), actualApis.get(1).name(), "API 2 name should match");

        // Verify that getRestApis was called on the mock client
        verify(mockApiGatewayClient).getRestApis(any(GetRestApisRequest.class));
    }
}
