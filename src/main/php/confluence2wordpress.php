<?php
/*
Plugin Name: Confluence XML-RPC Tools
Plugin URI: https://github.com/adutra/confluence2wordpress
Description: This Wordpress plugin brings additional XML-RPC APIs required for interaction with the "confluence2wordpress" Confluence plugin.
Version: ${project.version}
Author: Alexandre Dutra
Author URI: https://github.com/adutra
License: Apache License v. 2
*/

/*
Copyright 2011 Alexandre Dutra

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

require_once(ABSPATH . 'wp-admin/includes/plugin.php');
require_once(ABSPATH . 'wp-includes/post.php');

add_filter('xmlrpc_methods', 'c2w_add_xmlrpc_methods');

add_filter('upload_mimes', 'c2w_add_mime_types');

/**
 * Register new XML-RPC methods.
 * @param array $methods
 * @return array
 */
function c2w_add_xmlrpc_methods( $methods ) {
    $methods['c2w.getAuthors'] = 'c2w_get_authors';
    $methods['c2w.findPageIdBySlug'] = 'c2w_get_page_id_by_slug';
    $methods['c2w.uploadFile'] = 'c2w_upload_file';
    return $methods;
}

/**
 * Adds additional mime types to allowed mime types for uploaded files.
 * @see functions.php function wp_check_filetype()
 * @param array $mimes
 */
function c2w_add_mime_types( $mimes ) {
    $mimes['xml'] = 'text/xml';
    return $mimes;
}

/**
 * Find a page ID by its post slug.
 * Return null if post slug does not exist.
 * @param array $args
 * @return IXR_Error|number the page ID
 */
function c2w_get_page_id_by_slug($args){
	// Parse the arguments, assuming they're in the correct order
	
	//please see http://core.trac.wordpress.org/ticket/10513
	//WP version MUST be >= 2.9
	global $wp_xmlrpc_server;

	$wp_xmlrpc_server->escape($args);

	$blog_id	= (int) $args[0];
	$username	= $args[1];
	$password	= $args[2];
	$slug	    = $args[3];
	
    // Let's run a check to see if credentials are okay
	if ( !$user = $wp_xmlrpc_server->login($username, $password) ) {
		return $wp_xmlrpc_server->error;
	}
	
	if(!current_user_can("edit_posts")) {
		return(new IXR_Error(401, __("Sorry, you cannot access this API.")));
	}

	$page = get_page_by_path($slug, OBJECT, 'post');
	
	if($page){
		return (int) $page->ID;
	}
	
	return (int) null;
}

/**
 * Enhanced version of built-in method "wp.getAuthors".
 * @param array $args
 */
function c2w_get_authors($args) {
	
	// Parse the arguments, assuming they're in the correct order
	
	//please see http://core.trac.wordpress.org/ticket/10513
	//WP version MUST be >= 2.9
	global $wp_xmlrpc_server;

	$wp_xmlrpc_server->escape($args);

	$blog_id	= (int) $args[0];
	$username	= $args[1];
	$password	= $args[2];
	
    // Let's run a check to see if credentials are okay
	if ( !$user = $wp_xmlrpc_server->login($username, $password) ) {
		return $wp_xmlrpc_server->error;
	}

	if(!current_user_can("edit_posts")) {
		return(new IXR_Error(401, __("Sorry, you cannot access this API.")));
	}

	global $wpdb;
	
	//http://codex.wordpress.org/Roles_and_Capabilities#User_Level_to_Role_Conversion
	$sql = 
		"SELECT $wpdb->users.ID as user_id, user_login, user_nicename, display_name, ".
		"(SELECT meta_value from $wpdb->usermeta where $wpdb->usermeta.user_id = $wpdb->users.ID and $wpdb->usermeta.meta_key = 'first_name') as first_name, ".
		"(SELECT meta_value from $wpdb->usermeta where $wpdb->usermeta.user_id = $wpdb->users.ID and $wpdb->usermeta.meta_key = 'last_name') as last_name, ".
		"(SELECT meta_value from $wpdb->usermeta where $wpdb->usermeta.user_id = $wpdb->users.ID and $wpdb->usermeta.meta_key = 'wp_user_level') as user_level ".
		"FROM $wpdb->users" ;
	$rows = $wpdb->get_results($sql);
	
	$editors = array();
	foreach( (array) $rows as $row ) {
		$editors[] = array(
			"user_id"       => $row->user_id,
			"user_login"    => $row->user_login,
			"user_nicename" => $row->user_nicename,
			"display_name"  => $row->display_name,
			"first_name"    => $row->first_name,
			"last_name"     => $row->last_name,
			"user_level"    => $row->user_level
		);
	}

	return($editors);

}

/**
 * This is a copy of built-in method "mw_newMediaObject" in file class-wp-xmlrpc-server.php
 * that fixes the bug described here:
 * http://core.trac.wordpress.org/ticket/17604
 */
function c2w_upload_file($args) {
		//please see http://core.trac.wordpress.org/ticket/10513
		//WP version MUST be >= 2.9
		global $wp_xmlrpc_server;

		global $wpdb;

		$blog_ID     = (int) $args[0];
		$username  = $wpdb->escape($args[1]);
		$password   = $wpdb->escape($args[2]);
		$data        = $args[3];

		$name = sanitize_file_name( $data['name'] );
		$type = $data['type'];
		$bits = $data['bits'];

		logIO('O', '(MW) Received '.strlen($bits).' bytes');

		if ( !$user = $wp_xmlrpc_server->login($username, $password) )
			return $wp_xmlrpc_server->error;

		if ( !current_user_can('upload_files') ) {
			logIO('O', '(MW) User does not have upload_files capability');
			$wp_xmlrpc_server->error = new IXR_Error(401, __('You are not allowed to upload files to this site.'));
			return $wp_xmlrpc_server->error;
		}

		if ( $upload_err = apply_filters( 'pre_upload_error', false ) )
			return new IXR_Error(500, $upload_err);

		if ( !empty($data['overwrite']) && ($data['overwrite'] == true) ) {
			// Get postmeta info on the object.
			$old_file = $wpdb->get_row("
				SELECT ID
				FROM {$wpdb->posts}
				WHERE post_title = '{$name}'
					AND post_type = 'attachment'
			");

			// Delete previous file.
			wp_delete_attachment($old_file->ID);

		}

		$upload = wp_upload_bits($name, NULL, $bits);
		if ( ! empty($upload['error']) ) {
			$errorString = sprintf(__('Could not write file %1$s (%2$s)'), $name, $upload['error']);
			logIO('O', '(MW) ' . $errorString);
			return new IXR_Error(500, $errorString);
		}
		// Construct the attachment array
		// attach to post_id 0
		$post_id = 0;
		$attachment = array(
			'post_title' => $name,
			'post_content' => '',
			'post_type' => 'attachment',
			'post_parent' => $post_id,
			'post_mime_type' => $type,
			'guid' => $upload[ 'url' ]
		);

		// Save the data
		$id = wp_insert_attachment( $attachment, $upload[ 'file' ], $post_id );
		wp_update_attachment_metadata( $id, wp_generate_attachment_metadata( $id, $upload['file'] ) );

		return apply_filters( 'wp_handle_upload', array( 'file' => $name, 'url' => $upload[ 'url' ], 'type' => $type ), 'upload' );
	}

?>
