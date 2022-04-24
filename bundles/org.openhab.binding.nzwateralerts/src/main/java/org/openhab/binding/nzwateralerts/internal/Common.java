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
package org.openhab.binding.nzwateralerts.internal;

import static org.openhab.binding.nzwateralerts.internal.NZWaterAlertsBindingConstants.ERROR_PARSE;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class contains common functions.
 * 
 * Napier City Council
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class Common {
    public static Integer processResponse(String data) {
        return processResponse(data, 0);
    }

    public static int processResponse(String data, int noLevel) {
        switch (data.toLowerCase().trim()) {
            case "no":
            case "none":
                return noLevel;

            case "level one":
            case "one":
            case "1":
                return 1;

            case "level two":
            case "two":
            case "2":
                return 2;

            case "level three":
            case "three":
            case "3":
                return 3;

            case "level four":
            case "four":
            case "4":
                return 4;
        }
        return ERROR_PARSE;
    }
}
