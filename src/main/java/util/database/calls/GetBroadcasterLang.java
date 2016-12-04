package util.database.calls;

import util.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static util.database.Database.cleanUp;

/**
 * @author Veteran Software by Ague Mort
 */
public class GetBroadcasterLang {
    private Connection connection = Database.getInstance().getConnection();
    private PreparedStatement pStatement;
    private ResultSet result;

    public GetBroadcasterLang() {
        setConnection();
    }

    private void setConnection() {
        this.connection = Database.getInstance().getConnection();
    }

    private void setResult(ResultSet result) {
        this.result = result;
    }

    private void setStatement(String query) {
        try {
            if (connection.isClosed() || connection == null) {
                setConnection();
            }
            this.pStatement = connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized String action(String guildId) {
        try {
            String query = "SELECT `broadcasterLang` FROM `guild` WHERE `guildId` = ?";
            setStatement(query);
            pStatement.setString(1, guildId);
            setResult(pStatement.executeQuery());
            if (result.isBeforeFirst()) {
                if (result.next()) {
                    return result.getString("broadcasterLang");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cleanUp(pStatement, connection);
        }
        return null;
    }
}