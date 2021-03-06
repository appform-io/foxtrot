/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.foxtrot.server.resources;

import com.flipkart.foxtrot.common.Document;
import com.flipkart.foxtrot.common.group.GroupRequest;
import com.flipkart.foxtrot.common.group.GroupResponse;
import com.flipkart.foxtrot.core.TestUtils;
import com.flipkart.foxtrot.core.common.AsyncDataToken;
import com.flipkart.foxtrot.server.ResourceTestUtils;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by rishabh.goyal on 05/05/14.
 */
public class AsyncResourceTest extends FoxtrotResourceTest {

    @Rule
    public ResourceTestRule resources;

    public AsyncResourceTest() throws Exception {
        super();
        List<Document> documents = TestUtils.getGroupDocuments(getMapper());
        getQueryStore().save(TestUtils.TEST_TABLE_NAME, documents);
        getElasticsearchConnection().getClient()
                .indices()
                .refresh(new RefreshRequest("*"), RequestOptions.DEFAULT);
        resources = ResourceTestUtils.testResourceBuilder(getMapper())
                .addResource(new AsyncResource(getCacheManager()))
                .build();
    }

    @Test
    public void testGetResponse() throws Exception {
        GroupRequest groupRequest = new GroupRequest();
        groupRequest.setTable(TestUtils.TEST_TABLE_NAME);
        groupRequest.setNesting(Arrays.asList("os", "device", "version"));

        Map<String, Object> expectedResponse = new LinkedHashMap<String, Object>();

        final Map<String, Object> nexusResponse = new LinkedHashMap<String, Object>() {{
            put("1", 2);
            put("2", 2);
            put("3", 1);
        }};
        final Map<String, Object> galaxyResponse = new LinkedHashMap<String, Object>() {{
            put("2", 1);
            put("3", 1);
        }};
        expectedResponse.put("android", new LinkedHashMap<String, Object>() {{
            put("nexus", nexusResponse);
            put("galaxy", galaxyResponse);
        }});

        final Map<String, Object> nexusResponse2 = new LinkedHashMap<String, Object>() {{
            put("2", 1);
        }};
        final Map<String, Object> iPadResponse = new LinkedHashMap<String, Object>() {{
            put("2", 2);
        }};
        final Map<String, Object> iPhoneResponse = new LinkedHashMap<String, Object>() {{
            put("1", 1);
        }};
        expectedResponse.put("ios", new LinkedHashMap<String, Object>() {{
            put("nexus", nexusResponse2);
            put("ipad", iPadResponse);
            put("iphone", iPhoneResponse);
        }});

        AsyncDataToken dataToken = getQueryExecutor().executeAsync(groupRequest);
        await().pollDelay(1000, TimeUnit.MILLISECONDS).until(() -> true);

        GroupResponse groupResponse = resources
                .target("/v1/async/" + dataToken.getAction() + "/" + dataToken.getKey())
                .request()
                .get(GroupResponse.class);
        assertEquals(expectedResponse, groupResponse.getResult());
    }

    @Test
    public void testGetResponseInvalidAction() throws Exception {
        GroupRequest groupRequest = new GroupRequest();
        groupRequest.setTable(TestUtils.TEST_TABLE_NAME);
        groupRequest.setNesting(Arrays.asList("os", "device", "version"));

        AsyncDataToken dataToken = getQueryExecutor().executeAsync(groupRequest);
        await().pollDelay(1000, TimeUnit.MILLISECONDS).until(() -> true);
        GroupResponse response = resources
                .target(String.format("/v1/async/distinct/%s", dataToken.getKey()))
                .request()
                .get(GroupResponse.class);
        assertNull(response);
    }

    @Test
    public void testGetResponseInvalidKey() throws Exception {
        GroupRequest groupRequest = new GroupRequest();
        groupRequest.setTable(TestUtils.TEST_TABLE_NAME);
        groupRequest.setNesting(Arrays.asList("os", "device", "version"));

        AsyncDataToken dataToken = getQueryExecutor().executeAsync(groupRequest);
        await().pollDelay(1000, TimeUnit.MILLISECONDS).until(() -> true);

        GroupResponse response = resources
                .target(String.format("/v1/async/%s/dummy", dataToken.getAction()))
                .request()
                .get(GroupResponse.class);
        assertNull(response);
    }

    @Test
    public void testGetResponsePost() throws Exception {
        GroupRequest groupRequest = new GroupRequest();
        groupRequest.setTable(TestUtils.TEST_TABLE_NAME);
        groupRequest.setNesting(Arrays.asList("os", "device", "version"));

        Map<String, Object> expectedResponse = new LinkedHashMap<String, Object>();

        final Map<String, Object> nexusResponse = new LinkedHashMap<String, Object>() {{
            put("1", 2);
            put("2", 2);
            put("3", 1);
        }};
        final Map<String, Object> galaxyResponse = new LinkedHashMap<String, Object>() {{
            put("2", 1);
            put("3", 1);
        }};
        expectedResponse.put("android", new LinkedHashMap<String, Object>() {{
            put("nexus", nexusResponse);
            put("galaxy", galaxyResponse);
        }});

        final Map<String, Object> nexusResponse2 = new LinkedHashMap<String, Object>() {{
            put("2", 1);
        }};
        final Map<String, Object> iPadResponse = new LinkedHashMap<String, Object>() {{
            put("2", 2);
        }};
        final Map<String, Object> iPhoneResponse = new LinkedHashMap<String, Object>() {{
            put("1", 1);
        }};
        expectedResponse.put("ios", new LinkedHashMap<String, Object>() {{
            put("nexus", nexusResponse2);
            put("ipad", iPadResponse);
            put("iphone", iPhoneResponse);
        }});

        AsyncDataToken dataToken = getQueryExecutor().executeAsync(groupRequest);
        await().pollDelay(5000, TimeUnit.MILLISECONDS).until(() -> true);

        Entity<AsyncDataToken> asyncDataTokenEntity = Entity.json(dataToken);

        GroupResponse response = resources
                .target("/v1/async")
                .request()
                .post(asyncDataTokenEntity, GroupResponse.class);
        assertEquals(expectedResponse, response.getResult());
    }

    // TODO Not sure if returning no content is correct
    @Test
    public void testGetResponsePostInvalidKey() throws Exception {
        AsyncDataToken dataToken = new AsyncDataToken("group", null);
        Entity<AsyncDataToken> asyncDataTokenEntity = Entity.json(dataToken);
        GroupResponse response = resources
                .target("/v1/async")
                .request()
                .post(asyncDataTokenEntity, GroupResponse.class);
        assertNull(response);
    }

    @Test
    public void testGetResponsePostInvalidAction() throws Exception {
        AsyncDataToken dataToken = new AsyncDataToken(null, UUID.randomUUID()
                .toString());
        Entity<AsyncDataToken> asyncDataTokenEntity = Entity.json(dataToken);
        Response response = resources
                .target("/v1/async")
                .request()
                .post(asyncDataTokenEntity);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
}
