/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2016 Adobe
 * %%
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
 * #L%
 */
package com.adobe.acs.commons.wcm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        factory = "PageRootProviderConfig",
        service=PageRootProviderConfig.class)
@Designate(ocd=PageRootProviderConfig.Config.class,factory=true)
/**
 * Configuration instance for Page Root Provider.
 * Use service.ranking to guarantee priority between conflicting configurations.
 *
 * @see PageRootProviderMultiImpl
 */
public class PageRootProviderConfig {

    /* Default root. */
    static final String DEFAULT_PAGE_ROOT_PATH = "/content";
    
    @ObjectClassDefinition( name = "ACS AEM Commons - Page Root Provider Configuration",
            description = "Configuration instance for Page Root Provider, a service to fetch the site root page for a given resource.")
    public @interface Config {
        @AttributeDefinition(
                name = "Root page path pattern",
                description = "Regex(es) used to select the root page root path. Evaluates list top-down; first match wins. Defaults to [ " + DEFAULT_PAGE_ROOT_PATH + " ]",
                cardinality = Integer.MAX_VALUE,
                defaultValue = { DEFAULT_PAGE_ROOT_PATH })
        /* Page root property. */
        String[] page_root_path();
 
    }

    /* Page root property. */
    static final String PAGE_ROOT_PATH = "page.root.path";

    private static final Logger log = LoggerFactory.getLogger(PageRootProviderConfig.class);

    private List<Pattern> pageRootPatterns = null;

    /**
     * Retrieves the configured patterns.
     *
     * @return list of page root patterns.
     */
    public List<Pattern> getPageRootPatterns() {
        return this.pageRootPatterns;
    }

    @Activate
    protected void activate(Map<String, Object> props) {
        List<Pattern> patterns = new ArrayList<Pattern>();
        String[] regexes = PropertiesUtil.toStringArray(props.get(PAGE_ROOT_PATH), new String[] { DEFAULT_PAGE_ROOT_PATH });

        for(String regex : regexes) {
            try {
                Pattern p = Pattern.compile("^(" + regex + ")(|/.*)$");
                patterns.add(p);
                log.debug("Added Page Root Pattern [ {} ] to PageRootProvider", p.toString());
            } catch (Exception e) {
                log.error("Could not compile regex [ {} ] to pattern. Skipping...", regex, e);
            }
        }

        this.pageRootPatterns = Collections.unmodifiableList(patterns);
    }

    @Deactivate
    protected void deactivate() {
        if (this.pageRootPatterns != null) {
            for (Pattern p : this.pageRootPatterns) {
                log.debug("Removed Page Root Pattern [ {} ] from PageRootProvider", p.toString());
            }

            this.pageRootPatterns = null;
        }
    }

}
