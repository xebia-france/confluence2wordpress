/**
 * Copyright 2011-2012 Alexandre Dutra
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/**
 * 
 */
package fr.dutra.confluence2wordpress.core.converter.visitors;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;


/**
 * @author Alexandre Dutra
 *
 */
public class AttributesCleaner implements TagNodeVisitor {

    private static final String STYLE = "style";
    
    private static final String CLASS = "class";
    
	private static final String MARGIN_LEFT = "margin-left: 0.0px;";
	
	private static final String TEXT_ALIGN = "text-align: justify;";
	
	private static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

	private static final Joiner JOINER = Joiner.on(' ').skipNulls();
	
	private static final String C2W_PREFIX = "c2w";
	
	/**
     * @inheritdoc
     */
    public boolean visit(TagNode parentNode, HtmlNode htmlNode) {
        if (htmlNode instanceof TagNode) {
            TagNode tag = (TagNode) htmlNode;
            
            //filter css classes: allow only css classes starting with "c2w"
            //all others will be useless once on the Wordpress side
            String classes = tag.getAttributeByName(CLASS);
            if(classes != null){
				classes = filterCssClasses(classes);
				tag.setAttribute(CLASS, classes);
				classes = StringUtils.trimToNull(classes);
            	if(classes == null){
             		tag.removeAttribute(CLASS);
            	} else {
            		tag.setAttribute(CLASS, classes);
            	}
            }
            
            //remove "data-" HTML5 attributes
            Map<String, String> attributes = tag.getAttributes();
            Iterator<String> iterator = attributes.keySet().iterator();
            while (iterator.hasNext()) {
				String name = iterator.next();
				if(name.startsWith("data-")){
					iterator.remove();
				}
			}
            
            //style cleanup
            String style = tag.getAttributeByName(STYLE);
            if(style != null) {
            	style = style.replace(MARGIN_LEFT, "");
            	style = style.replace(TEXT_ALIGN, "");
            	style = StringUtils.trimToNull(style);
            	if(style == null){
             		tag.removeAttribute(STYLE);
            	} else {
            		tag.setAttribute(STYLE, style);
            	}
            }
        }
        return true;
    }

	private String filterCssClasses(String classes) {
		Iterable<String> tokens = SPLITTER.split(classes);
		Iterable<String> filtered = Iterables.filter(tokens, new Predicate<String>(){
			@Override
			public boolean apply(@Nullable String input) {
				return input.startsWith(C2W_PREFIX);
			}
		});
		String joined = JOINER.join(filtered);
		return joined;
	}

}