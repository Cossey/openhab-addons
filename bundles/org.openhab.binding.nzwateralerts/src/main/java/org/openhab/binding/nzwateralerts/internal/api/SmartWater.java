/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.nzwateralerts.internal.api;

import static org.openhab.binding.nzwateralerts.internal.NZWaterAlertsBindingConstants.ERROR_PARSE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartWater} class contains the logic to get data the
 * SmartWater.org.nz website.
 * 
 * Waikato Regional Council
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class SmartWater implements WaterWebService {
    private final Logger logger = LoggerFactory.getLogger(SmartWater.class);

    private static final String HOSTNAME = "http://www.smartwater.org.nz";
    private static final String REGION_HAMILTON = "/alert-levels/hamilton-city";
    private static final String REGION_WAITOMO = "/alert-levels/waitomo";
    private static final String REGION_WAIPA = "/alert-levels/waipa";

    private static final String PATTERN = "/assets/(?:.*?/)(?:water-alert-([0-4]).*?\\.svg).*?";
    private static final Pattern REGEX = Pattern.compile(PATTERN,
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    public String service() {
        return "smartwater";
    }

    @Override
    public String endpoint(final String region) {
        switch (region.toLowerCase()) {
            case "hamilton":
                return HOSTNAME + REGION_HAMILTON;

            case "waitomo":
                return HOSTNAME + REGION_WAITOMO;

            case "waipa":
                return HOSTNAME + REGION_WAIPA;
        }
        return "";
    }

    @Override
    public int findWaterLevel(final String data, final String area) {
        final Matcher matches = REGEX.matcher(data);

        while (matches.find()) {
            String level = matches.group(1);

            if ("no".equalsIgnoreCase(level) || "save".equalsIgnoreCase(level)) {
                logger.debug("Convert Data Level to 0");
                level = "0";
            }
            logger.trace("Data {}", level);

            return Integer.valueOf(level);
        }
        return ERROR_PARSE;
    }
}
