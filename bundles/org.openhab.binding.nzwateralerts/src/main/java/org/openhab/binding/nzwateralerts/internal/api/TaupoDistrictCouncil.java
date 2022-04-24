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
import org.openhab.binding.nzwateralerts.internal.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TaupoDistrictCouncil} class contains the logic to get data the
 * www.taupodc.govt.nz website.
 * 
 * Taupo District Council
 * 
 * NOTE: This is currently a placeholder. This region needs to go into restrictions
 * first so I can parse the correct output. -Stewart Cossey
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class TaupoDistrictCouncil implements WaterWebService {
    private final Logger logger = LoggerFactory.getLogger(TaupoDistrictCouncil.class);

    private static final String HOSTNAME = "https://www.taupodc.govt.nz";
    private static final String REGION_DISTRICT = "/transport-and-water/water-conservation";

    private static final String PATTERN = "<strong>(\\w+ )*restrictions</strong>";
    private static final Pattern REGEX = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

    @Override
    public String service() {
        return "taupodistrictcouncil";
    }

    @Override
    public String endpoint(final String region) {
        return HOSTNAME + REGION_DISTRICT;
    }

    @Override
    public int findWaterLevel(final String data, final String area) {
        final Matcher matches = REGEX.matcher(data);

        while (matches.find()) {
            final String level = matches.group(1);
            logger.debug("Data Level {}", level);

            int result = Common.processResponse(level);

            if (result != ERROR_PARSE) {
                return result;
            }

        }
        return ERROR_PARSE;
    }
}
