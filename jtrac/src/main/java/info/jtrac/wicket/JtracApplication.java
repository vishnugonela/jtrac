/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.wicket;

import info.jtrac.Jtrac;
import java.util.Locale;
import javax.servlet.ServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import wicket.protocol.http.WebApplication;
import wicket.resource.loader.IStringResourceLoader;

/**
 * main wicket application for jtrac
 * holds singleton service layer instance pulled from spring
 */
public class JtracApplication extends WebApplication {        
    
    private Jtrac jtrac;

    public Jtrac getJtrac() {
        return jtrac;
    }
    
    @Override
    public void init() {
        super.init();
        ServletContext sc = getWicketServlet().getServletContext();
        final WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(sc);        
        jtrac = (Jtrac) wac.getBean("jtrac");
        
        getResourceSettings().addStringResourceLoader(new IStringResourceLoader() {
            public String loadStringResource(Class clazz, String key, Locale locale, String style) {
                return wac.getMessage(key, null, locale);
            }
        });


    }    
    
    public Class getHomePage() {
        return DashboardPage.class;
    }    
    
}