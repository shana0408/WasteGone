package com.ntu.cz2006.wastegone.models;

/**
 User class store user's information
 @author ILoveNTU
 @version 2.1
 @since 2019-01-15
 */

public class User {
    /**
     * get user name
     */
    private String name;
    /**
     * get rewards
     */
    private int rewards;
    /**
     * get email
     */
    private String email;

    public User() {
    }
    /**
     * @return user name
     */
    public String getName() {
        return name;
    }

    /**
     * @return rewards
     */
    public int getRewards() {
        return rewards;
    }
    /**
     * @return email
     */
    public String getEmail() {
        return email;
    }
    /**
     * @param name set username
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @param rewards set rewards
     */
    public void setRewards(int rewards) {
        this.rewards = rewards;
    }
    /**
     * @param email set email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
