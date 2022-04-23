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
package org.openhab.binding.nzwateralerts.internal.handler;

import static org.openhab.binding.nzwateralerts.internal.NZWaterAlertsBindingConstants.*;
import static org.openhab.binding.nzwateralerts.internal.NZWaterAlertsBindingConstants.CHANNEL_ALERTLEVEL;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.nzwateralerts.internal.NZWaterAlertsConfiguration;
import org.openhab.binding.nzwateralerts.internal.api.WaterAlertWebClient;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NZWaterAlertsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class NZWaterAlertsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NZWaterAlertsHandler.class);

    private final int timeoutDefault = 3;
    private int timeoutRetry = timeoutDefault;
    private int refreshInterval = 5;
    private @Nullable NZWaterAlertsConfiguration config = null;
    private HttpClient httpClient;
    private @Nullable WaterAlertWebClient webClient;
    private @Nullable ScheduledFuture<?> future;

    public NZWaterAlertsHandler(Thing thing, HttpClient httpClient) {
        super(thing);

        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_ALERTLEVEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                update();
            }
        }
    }

    private void update() {
        final Integer waterLevel = webClient.getLevel();

        if (waterLevel == null || waterLevel < 0) {
            String errMessage = "Unable to get water level";
            switch (waterLevel) {
                case ERROR_TIMEOUT:
                    errMessage = "Request timeout";
                    break;
                case ERROR_PARSE:
                    errMessage = "Unable to parse response";
                    break;
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errMessage);
            startPolling(timeoutRetry);
        } else {
            updateStatus(ThingStatus.ONLINE);
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ALERTLEVEL), new DecimalType(waterLevel));
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(NZWaterAlertsConfiguration.class);

        if (config != null) {
            final String localLocation = config.location;
            if (localLocation == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No location configured");
            } else {
                this.webClient = new WaterAlertWebClient(httpClient, localLocation);
                refreshInterval = config.refreshInterval;
            }

            startPolling(0);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not create webClient, a parameter is null");
            logger.debug("Create Binder failed due to null config item");
        }
    }

    private synchronized void startPolling(int delay) {
        stopPolling();

        if (future == null || future.isCancelled()) {
            future = scheduler.scheduleWithFixedDelay(this::update, delay, refreshInterval, TimeUnit.HOURS);
        }
    }

    private synchronized void stopPolling() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }
}
