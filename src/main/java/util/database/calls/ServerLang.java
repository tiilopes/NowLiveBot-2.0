/*
 * Copyright 2016-2017 Ague Mort of Veteran Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package util.database.calls;

import util.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static util.database.Database.cleanUp;

/**
 * Created by keesh on 1/30/2017.
 */
public class ServerLang {

    private Connection connection;
    private PreparedStatement pStatement;
    private ResultSet result;
    private String langCode;

    public synchronized void setLang(String guildId, String language) {
        switch (language.toLowerCase()) {
            case "french":
                langCode = "fr";
                break;
            default: // English
                langCode = "en";
            break;
        }

        try {
            String query = "UPDATE `guild` SET `serverLang` = ? WHERE `guildId` = ?";
            if (connection == null || connection.isClosed()) {
                this.connection = Database.getInstance().getConnection();
            }
            this.pStatement = connection.prepareStatement(query);
            pStatement.setString(1, langCode);
            pStatement.setString(2, guildId);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cleanUp(pStatement, connection);
        }
    }

    public synchronized String getLangCode(String guildId) {
        try {
            String query = "SELECT `serverLang` FROM `guild` WHERE `guildId` = ?";
            if (connection == null || connection.isClosed()) {
                this.connection = Database.getInstance().getConnection();
            }
            this.pStatement = connection.prepareStatement(query);
            pStatement.setString(1, guildId);
            this.result = pStatement.executeQuery();

            while (result.next()) {
                return result.getString("serverLang");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cleanUp(result, pStatement, connection);
        }
        return null;
    }

}
