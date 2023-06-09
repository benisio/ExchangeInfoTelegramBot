package com.mycompany.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mycompany.Utilities;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Содержит статические методы, которые парсят json-ответы биржи в Map
 */
public class JsonReader {

    /**
     * Запрещаем создавать экземпляры класса, так как он состоит только из статичных методов.
     */
    private JsonReader() {}

    /**
     * Парсит в Map json-ответ Мосбиржи, содержащий данные за текущую или последнюю (если текущая уже закончилась)
     * торговую сессию. Пример такого json-ответа приведен в файле: src/example/moex_usd_rub_marketdata.json.
     * Код данного метода станет гораздо понятнее, если читать его, параллельно смотря в этот пример.
     * @param jsonResponse json-ответ Мосбиржи, содержащий данные за текущую или последнюю (если текущая уже
     *                     закончилась) торговую сессию.
     */
    public static Map<String, String> parseLastTradingDayDataToMap(String jsonResponse) {
        // парсить данные из json будем в промежуточные объекты типа List<String>
        Type listOfStrings = new TypeToken<List<String>>(){}.getType();

        JsonObject rootObject = JsonParser.parseString(jsonResponse).getAsJsonObject(); // получаем корневой объект json-ответа
        JsonObject marketdataObject = rootObject.get("marketdata").getAsJsonObject(); // из корневого объекта получаем объект "marketdata"

        JsonArray columnsArray = marketdataObject.get("columns").getAsJsonArray(); // из объекта "marketdata" получаем массив "columns"
        List<String> columnsList = new Gson().fromJson(columnsArray, listOfStrings); // парсим массив "columns" json-ответа в java-объект типа List<String>

        // из объекта "marketdata" получаем массив массивов "data", а из него получаем его единственный элемент - интересующий нас массив с данными
        JsonArray dataArray = marketdataObject.get("data").getAsJsonArray().get(0).getAsJsonArray();
        List<String> lastTradingDayDataList = new Gson().fromJson(dataArray, listOfStrings); // парсим этот массив из json-ответа в java-объект типа List<String>

        return Utilities.zipToMap(columnsList, lastTradingDayDataList); // объединяем два полученных List-а с данными в Map
    }

    /**
     * Парсит в Map json-ответ Мосбиржи, содержащий данные за предыдущие торговые сессии. Пример такого json-ответа
     * приведен в файле: src/example/moex_history_data.json. Код данного метода станет гораздо понятнее, если читать его,
     * параллельно смотря в этот пример.
     * @param jsonResponse json-ответ Мосбиржи, содержащий данные за предыдущие торговые сессии.
     */
    public static Map<String, String> parsePreviousDayDataToMap(String jsonResponse) {
        // парсить данные из json будем в промежуточные объекты типа List<String>
        Type listOfStrings = new TypeToken<List<String>>(){}.getType();

        JsonObject rootObject = JsonParser.parseString(jsonResponse).getAsJsonObject(); // получаем корневой объект json-ответа
        JsonObject historyObject = rootObject.get("history").getAsJsonObject(); // из корневого объекта получаем объект "history"

        JsonArray columnsArray = historyObject.get("columns").getAsJsonArray(); // из объекта "history" получаем массив "columns"
        List<String> columnsList = new Gson().fromJson(columnsArray, listOfStrings); // парсим массив "columns" json-ответа в java-объект типа List<String>

        // Из объекта "history" получаем массив массивов "data", а из него получаем его 0-ой элемент - массив с
        // результатами торгов предыдущей торговой сессии. В массиве "data" данные за предыдущую торговую сессию лежат
        // на первом месте потому, что так составлен запрос: в нем указан параметр, согласно которому данные в ответе
        // сортируются по дате торгов по убыванию.
        JsonArray dataArray = historyObject.get("data").getAsJsonArray();

        List<String> previousDayDataList = new Gson().fromJson(dataArray.get(0), listOfStrings); // данные за предпоследнюю торговую сессию

        return Utilities.zipToMap(columnsList, previousDayDataList); // объединяем два полученных List-а с данными в Map
    }

    /**
     * Парсит json-ответ биржи Bybit, содержащий данные торгов криптовалютной пары. Пример такого json-ответа приведен
     * в файле: src/example/bybit_wlkn_usdt_marketdata.json. Код данного метода станет гораздо понятнее, если читать
     * его, параллельно смотря в этот пример.
     * @param jsonResponse json-ответ биржи Bybit с данными торгов криптовалютной пары
     * @return Map с распарсенными данными объекта "list" json-ответа
     */
    public static Map<String, String> parseBybitMarketDataToMap(String jsonResponse) {
        // задаем тип переменной, в которую будут парсится данные из json-ответа, равным LinkedHashMap<String, String>
        Type type = new TypeToken<LinkedHashMap<String, String>>(){}.getType();

        // получаем json-ответ в виде JsonObject
        JsonObject bybitResponse = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonObject result = bybitResponse.get("result").getAsJsonObject(); // из bybitResponse получаем свойство "result" типа JsonObject
        JsonArray list = result.get("list").getAsJsonArray(); // из result получаем свойство "list" типа JsonArray, так как в нем лежат данные, которые нам нужно распарсит

        // парсим содержимое list в ap. парсим из list.get(0), так как list - это массив типа JsonArray,
        // состоящий из одного единственного элемента, таким образом мы получаем этот элемент и вот уже из него парсим
        Map<String, String> marketData = new Gson().fromJson(list.get(0), type);

        return marketData;
    }
}