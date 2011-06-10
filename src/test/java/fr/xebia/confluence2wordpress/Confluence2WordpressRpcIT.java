/**
 * Copyright 2011 Alexandre Dutra
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.confluence2wordpress;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import junit.framework.Assert;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.junit.Test;


public class Confluence2WordpressRpcIT extends AbstractIT {

    protected XmlRpcClient client;

    protected String token;

    @Test
    public void testXmlRpc() throws IOException, XmlRpcException {
        String content = convertWikiToHtml();
        Assert.assertNotNull(content);
        System.out.println(content);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getConfluenceWebTester().getXmlRpcClient();
        token = loginXmlRpc();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Assert.assertTrue("Logout failed", logoutXmlRpc());
    }

    protected String loginXmlRpc() throws XmlRpcException, IOException {
        XmlRpcRequest loginRequest = new XmlRpcRequest("confluence1.login", new Vector<Object>(Arrays.asList("admin","admin")));
        String token = (String) client.execute(loginRequest);
        Assert.assertNotNull("Login failed", token);
        Assert.assertFalse("Login failed", "".equals(token));
        return token;
    }


    protected String convertWikiToHtml() throws XmlRpcException, IOException {
        XmlRpcRequest request = new XmlRpcRequest("confluence2wordpress.convert",
            new Vector<Object>(
                Arrays.asList(
                    token, Long.toString(pageId), new Hashtable<Object,Object>())));
        String content = (String) client.execute(request);
        return content;
    }

    protected boolean logoutXmlRpc() throws XmlRpcException, IOException {
        XmlRpcRequest loginRequest = new XmlRpcRequest("confluence1.logout", new Vector<Object>(Arrays.asList(token)));
        Boolean logout = (Boolean) client.execute(loginRequest);
        return logout;
    }



}