/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.flipkart.foxtrot.core.querystore.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com) Date: 14/09/13 Time: 2:12 PM
 */
@NoArgsConstructor
public class ClusterConfig {

    @JsonProperty("name")
    @Valid
    @NotNull
    @NotEmpty
    private String name = null;

    @JsonProperty("discovery")
    private ClusterDiscoveryConfig discovery;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClusterDiscoveryConfig getDiscovery() {
        return discovery;
    }

    public void setDiscovery(ClusterDiscoveryConfig discovery) {
        this.discovery = discovery;
    }
}
