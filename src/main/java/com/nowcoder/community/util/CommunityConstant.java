package com.nowcoder.community.util;

public interface CommunityConstant {

    /**
     * Activation success
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * Repeated activation
     */
    int REPEATED_ACTIVATION = 1;

    /**
     * Activation failure
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * Default expiration time for login credential
     */
    int DEFAULT_EXPIRATION_SECONDS = 3600 * 12;

    /**
     * Expiration time for login credential if remembered
     */
    int REMEMBERED_EXPIRATION_SECONDS = 3600 * 12 * 100;

    /**
     * Entity type: post
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * Entity type: comment
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * Entity type: user
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * topic: comment
     */
    String TOPIC_COMMENT = "comment";

    /**
     * topic: like
     */
    String TOPIC_LIKE = "like";

    /**
     * topic: follow
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * topic: follow
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * topic: delete
     */
    String TOPIC_DELETE = "delete";

    /**
     * topic: share
     */
    String TOPIC_SHARE = "share";

    /**
     * User ID for System
     */
    int SYSTEM_USER_ID = 1;

    /**
     * auth: user
     */
    String AUTHORITY_USER = "user";

    /**
     * auth: admin
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * auth: moderator
     */
    String AUTHORITY_MODERATOR = "moderator";
}
