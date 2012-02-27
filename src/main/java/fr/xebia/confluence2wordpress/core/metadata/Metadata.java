/**
 * Copyright 2011 Alexandre Dutra
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
package fr.xebia.confluence2wordpress.core.metadata;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.xebia.confluence2wordpress.wp.WordpressPost;

/**
 * @author Alexandre Dutra
 *
 */
public class Metadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @MetadataItem
    private Integer postId;
    
    @MetadataItem
    private boolean draft = true;

    @MetadataItem
    private String postSlug;

    @MetadataItem
    private Integer authorId;

    @MetadataItem
    private List<String> categoryNames;

    @MetadataItem
    private List<String> tagNames;

    @MetadataItem
    private String pageTitle;

    @MetadataItem
    private List<String> ignoredConfluenceMacros;

    @MetadataItem
    private Map<String,String> tagAttributes;

    @MetadataItem
    private boolean optimizeForRDP;

    @MetadataItem
    private boolean includeTOC;
    
    @MetadataItem    
    private String permalink;

    @MetadataItem    
    private String digest;
    
    @MetadataItem    
    private Date dateCreated;

    @MetadataItem
    private Map<String,Integer> attachments;

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public List<String> getIgnoredConfluenceMacros() {
        return ignoredConfluenceMacros;
    }

    public void setIgnoredConfluenceMacros(List<String> ignoredConfluenceMacros) {
        this.ignoredConfluenceMacros = ignoredConfluenceMacros;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getPostSlug() {
        return postSlug;
    }

    public void setPostSlug(String postSlug) {
        this.postSlug = postSlug;
    }
    
    public Integer getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }
    
    public List<String> getCategoryNames() {
        return categoryNames;
    }

    
    public void setCategoryNames(List<String> categoryNames) {
        this.categoryNames = categoryNames;
    }

    
    public List<String> getTagNames() {
        return tagNames;
    }

    
    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }

    
    public boolean isDraft() {
        return draft;
    }

    
    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    
    public boolean isOptimizeForRDP() {
        return optimizeForRDP;
    }

    
    public void setOptimizeForRDP(boolean optimizeForRDP) {
        this.optimizeForRDP = optimizeForRDP;
    }

    
    public String getPermalink() {
        return permalink;
    }

    
    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    
    public boolean isIncludeTOC() {
        return includeTOC;
    }

    
    public void setIncludeTOC(boolean includeTOC) {
        this.includeTOC = includeTOC;
    }
    
    
    public String getDigest() {
        return digest;
    }

    
    public void setDigest(String digest) {
        this.digest = digest;
    }

    
    public Date getDateCreated() {
        return dateCreated;
    }

    
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Map<String, String> getTagAttributes() {
		return tagAttributes;
	}

	public void setTagAttributes(Map<String, String> tagAttributes) {
		this.tagAttributes = tagAttributes;
	}

	public Map<String, Integer> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, Integer> attachments) {
		this.attachments = attachments;
	}

	public WordpressPost createPost() {
        WordpressPost post = new WordpressPost();
        post.setDraft(this.isDraft());
        post.setPostId(this.getPostId());
        post.setAuthorId(this.getAuthorId());
        post.setTitle(this.getPageTitle());
        post.setPostSlug(this.getPostSlug());
        post.setDateCreated(this.getDateCreated());
        if(this.getCategoryNames()!=null){
            post.setCategoryNames(this.getCategoryNames()); //categories must exist.
        }
        if(this.getTagNames()!= null){
            post.setTagNames(this.getTagNames()); //tags are dynamically created.
        }
        return post;
    }
    
    public void updateFromPost(WordpressPost post) {
        this.setPostId(post.getPostId());
        this.setDraft(post.isDraft());
        this.setPageTitle(post.getTitle());
        this.setPostSlug(post.getPostSlug());
        this.setAuthorId(post.getAuthorId());
        this.setDateCreated(post.getDateCreated());
        this.setCategoryNames(post.getCategoryNames());
        this.setTagNames(post.getTagNames());
        String permalink = post.getLink();
		//it seems that WP does not always send back the query string
		//containing "preview=true"...
		if(post.isDraft() && ! permalink.contains("preview=true")) {
			if(permalink.contains("?")){
	    		permalink += "&preview=true";
			} else {
	    		permalink += "?preview=true";
			}
        }
        this.setPermalink(permalink);
        this.setDigest(post.getDigest());
    }


}