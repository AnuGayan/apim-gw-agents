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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AWSAPIDiscoveryServiceTest {

    @Mock
    private AWSGatewayDeployer mockAwsGatewayDeployer;

    @InjectMocks
    private AWSAPIDiscoveryService awsApiDiscoveryService;

    @Test
    void testDiscoverAPIs() throws APIManagementException {
        // Prepare a list of API names to be returned by the mock deployer
        List<String> expectedApiNames = Arrays.asList("API A", "API B", "API C");

        // Mock the behavior of AWSGatewayDeployer's discoverAPIs method
        when(mockAwsGatewayDeployer.discoverAPIs()).thenReturn(expectedApiNames);

        // Call the method to be tested
        List<String> actualApiNames = awsApiDiscoveryService.discoverAPIs();

        // Verify the results
        assertEquals(expectedApiNames.size(), actualApiNames.size(), "Number of API names should match");
        assertEquals(expectedApiNames.get(0), actualApiNames.get(0), "First API name should match");
        assertEquals(expectedApiNames.get(1), actualApiNames.get(1), "Second API name should match");
        assertEquals(expectedApiNames.get(2), actualApiNames.get(2), "Third API name should match");


        // Verify that discoverAPIs was called on the mock deployer
        verify(mockAwsGatewayDeployer).discoverAPIs();
    }

    @Test
    void testDiscoverAPIs_whenDeployerThrowsException() throws APIManagementException {
        // Mock the behavior of AWSGatewayDeployer's discoverAPIs method to throw an exception
        when(mockAwsGatewayDeployer.discoverAPIs()).thenThrow(new APIManagementException("Test Exception"));

        // Call the method to be tested and assert that it throws the expected exception
        try {
            awsApiDiscoveryService.discoverAPIs();
        } catch (APIManagementException e) {
            assertEquals("Test Exception", e.getMessage(), "Exception message should match");
        }

        // Verify that discoverAPIs was called on the mock deployer
        verify(mockAwsGatewayDeployer).discoverAPIs();
    }
}
