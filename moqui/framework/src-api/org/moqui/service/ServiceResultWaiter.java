/*
 * Copyright 2010 David E. Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.moqui.service;

import java.util.Map;

/**
 * ServiceFacade Result Waiter Class
 */
public class ServiceResultWaiter implements ServiceRequester {

    public static final String module = ServiceResultWaiter.class.getName();

    /** Status code for a running service */
    public static final int SERVICE_RUNNING = -1;
    /** Status code for a failed service */
    public static final int SERVICE_FAILED = 0;
    /** Status code for a successful service */
    public static final int SERVICE_FINISHED = 1;

    private boolean completed = false;
    private int status = -1;
    private Map<String, Object> result = null;
    private Throwable t = null;

    /**
     * @see org.moqui.service.ServiceRequester#receiveResult(java.util.Map)
     */
    public synchronized void receiveResult(Map<String, Object> result) {
        this.result = result;
        completed = true;
        status = SERVICE_FINISHED;
        notify();
    }

    /**
     * @see org.moqui.service.ServiceRequester#receiveThrowable(java.lang.Throwable)
     */
    public synchronized void receiveThrowable(Throwable t) {
        this.t = t;
        completed = true;
        status = SERVICE_FAILED;
        notify();
    }

    /**
     * Returns the status of the service.
     * @return int Status code
     */
    public synchronized int status() {
        return this.status;
    }

    /**
     * If the service has completed return true
     * @return boolean
     */
    public synchronized boolean isCompleted() {
        return completed;
    }

    /**
     * Returns the exception which was thrown or null if none
     * @return Exception
     */
    public synchronized Throwable getThrowable() {
        if (!isCompleted()) {
            throw new java.lang.IllegalStateException("Cannot return exception, service has not completed.");
        }
        return this.t;
    }

    /**
     * Gets the results of the service or null if none
     * @return Map
     */
    public synchronized Map<String, Object> getResult() {
        if (!isCompleted()) {
            throw new java.lang.IllegalStateException("Cannot return result, service has not completed.");
        }
        return result;
    }

    /**
     * Waits for the service to complete
     * @return Map
     */
    public synchronized Map<String, Object> waitForResult() {
        return this.waitForResult(10);
    }

    /**
     * Waits for the service to complete, check the status every n milliseconds
     * @param milliseconds
     * @return Map
     */
    public synchronized Map<String, Object> waitForResult(long milliseconds) {
        while (!isCompleted()) {
            try {
                this.wait(milliseconds);
            } catch (java.lang.InterruptedException e) {
                // TODO: log something here, once we get the logging stuff in place
            }
        }
        return this.getResult();
    }
}

