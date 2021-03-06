/**
 * (c) Copyright HCL Technologies Ltd. 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.bamboo.plugin.util;

import com.hcl.appscan.bamboo.plugin.impl.LogHelper;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;

import java.io.Serializable;

public class ScanProgress implements IProgress, Serializable {
    private LogHelper logger;

    public ScanProgress(LogHelper logger) {
        this.logger = logger;
    }

    @Override
    public void setStatus(Message message) {
        setStatus(message , null);
    }

    @Override
    public void setStatus(Throwable throwable) {
        logger.error(throwable.getLocalizedMessage() != null ? throwable.getLocalizedMessage() : throwable.toString());
    }

    @Override
    public void setStatus(Message message, Throwable throwable) {
        String m = throwable == null ? logger.getText(message.getText()) : logger.getText(message.getText(), throwable);
        if (message.getSeverity() == Message.INFO) {
            logger.info(m);
        } else if (message.getSeverity() == Message.ERROR) {
            logger.error(m);
        } else logger.debug(m);
    }
}
