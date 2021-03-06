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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;


/**
 * @author Alexandre Dutra
 *
 */
public class EmptyTagRemover implements TagNodeVisitor {

	private final List<String> tagNames;
	
	public EmptyTagRemover(List<String> tagNames) {
		this.tagNames = tagNames;
	}

	@Override
	public boolean visit(TagNode parentNode, HtmlNode htmlNode) {
		if (htmlNode instanceof TagNode) {
            TagNode tag = (TagNode) htmlNode;
            if(canBeRemoved(tag)) {
            	removeTag(parentNode, tag);
            }
        }
        return true;
	}

    private boolean canBeRemoved(TagNode tag) {
		return tagNames.contains(tag.getName()) && hasNoAttributes(tag) && hasNoText(tag);
	}

	private boolean hasNoAttributes(TagNode tag) {
        Collection<String> values = tag.getAttributes().values();
        for (String value : values) {
            if(StringUtils.isNotBlank(value)){
                return false;
            }
        }
        return true;
    }

	private boolean hasNoText(TagNode tag) {
        StringBuffer sb = tag.getText();
        return fr.dutra.confluence2wordpress.util.StringUtils.isWhitespace(sb);
    }

	private void removeTag(TagNode parentNode, TagNode tag) {
		parentNode.removeChild(tag);
	}

}
