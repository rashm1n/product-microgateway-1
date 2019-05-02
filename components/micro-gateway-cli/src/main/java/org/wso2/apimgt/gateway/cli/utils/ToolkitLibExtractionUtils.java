/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.apimgt.gateway.cli.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.apimgt.gateway.cli.constants.GatewayCliConstants;
import org.wso2.apimgt.gateway.cli.exception.CLIInternalException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.wso2.apimgt.gateway.cli.utils.GatewayCmdUtils.getResourceFolderLocation;

/**
 * This class represents the utility functions required for library packages extraction
 */
public class ToolkitLibExtractionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolkitLibExtractionUtils.class);

    /**
     * Extracts the platform and runtime and copy related jars and balos to extracted runtime and platform.
     */
    public static void extractPlatformAndRuntime() {
        try {
            String libPath = GatewayCmdUtils.getCLILibPath();
            String baloPath = GatewayCliConstants.CLI_GATEWAY + File.separator + GatewayCliConstants.CLI_BALO;
            String breLibPath = GatewayCliConstants.CLI_BRE + File.separator + GatewayCliConstants.CLI_LIB;
            String runtimeExtractedPath = libPath + File.separator + GatewayCliConstants.CLI_RUNTIME;
            String platformExtractedPath =
                    GatewayCmdUtils.getCLILibPath() + File.separator + GatewayCliConstants.CLI_PLATFORM;

            extractBallerinaDist(platformExtractedPath, libPath, baloPath, breLibPath, true);
            extractBallerinaDist(runtimeExtractedPath, libPath, baloPath, breLibPath, false);

            //get the platform .p12 path
            String platformTruststorePath = platformExtractedPath + File.separator + GatewayCliConstants.CLI_BRE +
                    File.separator + GatewayCliConstants.SECURITY + File.separator +
                    GatewayCliConstants.BALLERINA_TRUSTSTORE;
            File platformTruststoreFile = new File(platformTruststorePath);

            //get the runtime .p12 path
            String runtimeTruststorePath = runtimeExtractedPath + File.separator + GatewayCliConstants.CLI_BRE +
                    File.separator + GatewayCliConstants.SECURITY + File.separator +
                    GatewayCliConstants.BALLERINA_TRUSTSTORE;
            File runtimeTruststoreFile = new File(runtimeTruststorePath);

            String resourceTrustStorePath = getResourceFolderLocation() + File.separator +
                    GatewayCliConstants.BALLERINA_TRUSTSTORE;
            File resourceTruststoreFile = new File(resourceTrustStorePath);

            if (resourceTruststoreFile.exists()) {
                if (platformTruststoreFile.exists()) {
                    platformTruststoreFile.delete();
                }
                FileUtils.copyFile(resourceTruststoreFile, platformTruststoreFile);

                if (runtimeTruststoreFile.exists()) {
                    runtimeTruststoreFile.delete();
                }
                FileUtils.copyFile(resourceTruststoreFile, runtimeTruststoreFile);
            } else {
                throw new CLIInternalException("Truststore resource is not found");
            }
        } catch (IOException e) {
            String message = "Error while unzipping platform and runtime while project setup";
            LOGGER.error(message, e);
            throw new CLIInternalException(message);
        }
    }

    private static void extractBallerinaDist(String destination, String libPath, String baloPath, String breLibPath,
                                             Boolean isAddToClasspath) throws IOException {
        if (!Files.exists(Paths.get(destination))) {
            ZipUtils.unzip(destination + GatewayCliConstants.EXTENSION_ZIP, destination,
                    isAddToClasspath);

            // Copy balo to the platform
            GatewayCmdUtils.copyFolder(libPath + File.separator + baloPath,
                    destination + File.separator + GatewayCliConstants.CLI_LIB + File.separator
                            + GatewayCliConstants.CLI_REPO);

            // Copy gateway jars to platform
            GatewayCmdUtils.copyFolder(libPath + File.separator + GatewayCliConstants.CLI_GATEWAY + File.separator
                    + GatewayCliConstants.CLI_PLATFORM, destination + File.separator + breLibPath);
        }
    }
}
