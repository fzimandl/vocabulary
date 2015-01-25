/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dict;

import com.google.common.base.CharMatcher;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author filip
 */
public class Dict {

    private static final String[] ALPHABET = new String[] {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
        "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    };

    static String getExcludedLeter(String inputChar) {
        //return "(instr(dict.word,'" + inputChar + "') > 0)";
        return "(word not like '%" + inputChar + "%')";
    }

    static String getIncludedLeter(String inputChar, int times) {
        String condition = "(word not like '%";
        for (int i = 0; i <= times; i++) {
            condition = condition.concat(inputChar + "%");
        }
        return condition + "')";
    }

    /**
     *
     * @param excludedChars
     * @param includedChars
     * @param maxLength
     * @return Select statement
     */
    static String constructSelect(List<String> excludedChars, HashMap<String, Integer> includedChars, int maxLength) {
        String select = "select * from dict where (";
        for (String oneLeter : excludedChars) {
            String excludedLeterSql = getExcludedLeter(oneLeter);
            select = select.concat(excludedLeterSql).concat(" and ");
        }
        Set<String> keySet = includedChars.keySet();
        for (String key : keySet) {
            Integer howManyTimes = includedChars.get(key);
            String includedLeterSql = getIncludedLeter(key, howManyTimes);
            select = select.concat(includedLeterSql).concat(" and ");
        }
        select = select.concat("(length(trim(dict.word)) <" + (maxLength + 1) + "))");
        return select;
    }

    public static void main(String[] args) throws SQLException {

        //If a char is not in input, it should be in select...where...not like...
        String availableChars = args[0];
        List<String> letersToSelect = new ArrayList();
        for (String leter: ALPHABET) {
            if (!availableChars.contains(leter)) {
                letersToSelect.add(leter);
            }
        }

        //Indentify multiple occurance of the same character
        HashMap charMap = new HashMap();
        for (char oneChar: availableChars.toCharArray()) {
            int countIn = CharMatcher.is(oneChar).countIn(availableChars);
            charMap.put(String.valueOf(oneChar), countIn);
        }
        //Construct select statement
        String select = constructSelect(letersToSelect, charMap, availableChars.length());

        //URL of Oracle database server
        String url = "jdbc:oracle:thin:@192.168.56.101:1521:ORCL";

        //properties for creating connection to Oracle database
        Properties props = new Properties();
        props.setProperty("user", "dictionary");
        props.setProperty("password", "dictionary");

        //creating connection to Oracle database using JDBC
        Connection conn = DriverManager.getConnection(url, props);
        PreparedStatement preStatement = conn.prepareStatement(select);
        ResultSet result;

        result = preStatement.executeQuery();
        while (result.next()) {
            System.out.println("Scrabble word: " + result.getString("word"));
        }

        System.out.println("done");

    }

}
