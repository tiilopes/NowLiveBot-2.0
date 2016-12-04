package platform.beam.controller;

import com.google.common.util.concurrent.ListenableFuture;
import platform.generic.controller.PlatformController;
import pro.beam.api.BeamAPI;
import pro.beam.api.resource.channel.BeamChannel;
import pro.beam.api.response.channels.ChannelStatusResponse;
import pro.beam.api.services.impl.ChannelsService;
import util.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static util.database.Database.cleanUp;

/**
 * @author Veteran Software by Ague Mort
 */
public class BeamController extends BeamAPI {

    private static BeamAPI beamChannel;
    private static Connection connection;
    private static PreparedStatement pStatement;
    private static ResultSet result;
    private PlatformController pController = new PlatformController();

    public static synchronized List<String> checkFilters(String guildId) {
        try {
            String query = "SELECT * FROM `filter` WHERE `guildId` = ?";

            connection = Database.getInstance().getConnection();
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, guildId);
            result = pStatement.executeQuery();

            List<String> filters = new ArrayList<>();

            if (result.isBeforeFirst()) {
                while (result.next()) {
                    filters.add(result.getString("name"));
                }
                return filters;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cleanUp(result, pStatement, connection);
        }

        return null;
    }

    public static synchronized Boolean channelExists(String channelName) {

        beamChannel = new BeamAPI();

        ListenableFuture<BeamChannel> channel = beamChannel.use(ChannelsService.class).findOneByToken(channelName);

        try {
            if (channel.get().online || !channel.get().online) {
                return true;
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (ExecutionException ee) {
            // Typically thrown when the channel does not exist along with a HttpBadResponseException
            return false;
        }

        return false;
    }

    public final synchronized void checkChannel(String channelName, String guildId, Integer platformId) {
        // Instantiate
        beamChannel = new BeamAPI();

        ListenableFuture<BeamChannel> channel = beamChannel.use(ChannelsService.class).findOneByToken(channelName);
        try {
            Map<String, String> args = new HashMap<>();
            args.put("guildId", guildId);
            args.put("channelName", channelName);

            if (channel.get().online) { // if the channel is online

                // Check to see if the game name is not empty
                if (!channel.get().type.name.isEmpty() && channel.get().type.name != null) {

                    // Grab the game name
                    String gameName = channel.get().type.name;

                    // Grab any entered filters
                    List<String> filters = checkFilters(guildId);

                    if (filters != null) {
                        for (String filter : filters) {
                            if (gameName.equalsIgnoreCase(filter)) {
                                pController.onlineStreamHandler(setArgs(args, channel), platformId);
                            }
                        }
                    } else {
                        pController.onlineStreamHandler(setArgs(args, channel), platformId);
                    }
                }
            } else {
                pController.offlineStreamHandler(args, platformId);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> setArgs(Map<String, String> args, ListenableFuture<BeamChannel> channel) {
        try {
            args.put("channelName", channel.get().token);
            args.put("streamTitle", channel.get().name);
            args.put("gameName", channel.get().type.name);
            args.put("url", "https://beam.pro/" + args.get("channelName"));
            args.put("thumbnail", channel.get().thumbnail.url);
            args.put("banner", channel.get().cover.url);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return args;
    }

}