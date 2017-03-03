package com.jaredscarito.charitytwitterbot.main;

import org.bukkit.configuration.file.YamlConfiguration;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by TheWolfBadger on 2/21/17.
 */
public class CharityTwitterBot {
        private static Twitter twitter;
        private static File infoFile = new File("info.yml");
        public static void main(String args[]) throws TwitterException {
            if(!infoFile.exists()) {
                try {
                    infoFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            YamlConfiguration configY = YamlConfiguration.loadConfiguration(infoFile);
            if(!configY.contains("Oauth")) {
                //Set defaults up
                configY.set("Oauth.ConsumerKey", "");
                configY.set("Oauth.ConsumerSecret", "");
                configY.set("Oauth.accesstoken", "");
                configY.set("Oauth.accesstokensecret", "");
                configY.set("Keywords", new ArrayList<>());
                try {
                    configY.save(infoFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true);
            cb.setOAuthConsumerKey(configY.getString("Oauth.ConsumerKey"));
            cb.setOAuthConsumerSecret(configY.getString("Oauth.ConsumerSecret"));
            cb.setOAuthAccessToken(configY.getString("Oauth.AccessToken"));
            cb.setOAuthAccessTokenSecret(configY.getString("Oauth.AccessTokenSecret"));
            Configuration conf = cb.build();
            twitter = new TwitterFactory(conf).getInstance();
            System.out.println("Running it");
            final int[] tweetsInfo = {0, 100}; // Tweeted, AllowedPerHour
            StatusListener listener = new StatusListener() {

            public void onStatus(Status status) {
                try {
                    // System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                    if(status.getUser().getId() !=twitter.getId()) {
                        if (!status.isRetweet()) {
                            String text = status.getText().toLowerCase();
                            int retweetedAlready = 0;
                            for(String keywords : configY.getStringList("Keywords")) {
                                if(text.contains(keywords.toLowerCase()) && retweetedAlready == 0) {
                                    if (!(status.getUserMentionEntities().length >= 2)) {
                                        if (tweetsInfo[1] <= tweetsInfo[0]) {
                                            System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                                            //TODO Make this a randomized message
                                            //StatusUpdate stat = new StatusUpdate("Thanks for the tag! @" + status.getUser().getScreenName());
                                            //stat.setInReplyToStatusId(status.getId());
                                            //twitter.updateStatus(stat);
                                            twitter.createFavorite(status.getId());
                                            twitter.retweetStatus(status.getId());
                                            tweetsInfo[0]++;
                                            twitter.createFriendship(status.getUser().getId());
                                            retweetedAlready = 1;
                                            //TODO Put this in a direct message received shit
                                            /** /
                                             twitter.sendDirectMessage(status.getUser().getId(),
                                             "Hello there, I am trying to win this contest for charity! If I win please just message me back with 'won' " +
                                             "and I will let you know the details to get the package to me! Thanks!");
                                             /**/
                                        } else {
                                            try {
                                                tweetsInfo[0] = 0;
                                                Thread.sleep(1000 * 60 * 60);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }
            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }
            @Override
            public void onStallWarning(StallWarning stallWarning) {
            }
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        FilterQuery fq = new FilterQuery();
        fq.track("Contest", "RT", "Giveaway");
        TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
        twitterStream.addListener(listener);
        twitterStream.filter(fq);
        //Now let's see if it works correctly
        //twitter.updateStatus("Hello there, I am working!");
        /** /
         try {
         System.out.print("Sleeping now");
         Thread.sleep(40);
         } catch (InterruptedException e) {
         e.printStackTrace();
         }
         System.out.print("No longer sleeping");
         /**/
    }
}
