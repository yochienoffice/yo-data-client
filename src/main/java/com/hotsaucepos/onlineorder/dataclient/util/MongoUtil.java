package com.hotsaucepos.onlineorder.dataclient.util;

import com.hotsaucepos.onlineorder.dataclient.model.Data;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoUtil {
    private final String DEV = "mongodb+srv://hs-admin:utG4QTGDI4rbravp@online-ordering-test-lpzy5.mongodb.net/test?retryWrites=true&w=majority";
    private final String PROD = "mongodb+srv://hs-admin:jAkNU8DgAvlrziM6@online-ordering-gscnq.mongodb.net/test?retryWrites=true&w=majority";
    private final String uri = PROD;
    public MongoCollection<Document> collection;
    private String[] requestOrderStatus = new String[]{"received", "ready", "completed"};
    public Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
    public MongoDatabase mongoDatabase;
    public List<Long> storeList = new ArrayList<>();
    public void createConnection() {
        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient mongoClient = new MongoClient(clientURI);
        String devDb = "online-ordering-test";
        String prodDb = "online-ordering";
        mongoDatabase = mongoClient.getDatabase(prodDb);
        collection = mongoDatabase.getCollection("Order");
        mongoLogger.setLevel(Level.SEVERE);
        storeList = getStoresFromDatabase();
    }

    public List<Long> deleteDataFromDatabase(String startDateString, String endDateString, Long storeId) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().toZoneId());
        DateFormat df = new SimpleDateFormat(pattern); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);

        Instant startInstant = Instant.parse(df.format(new Date(startDateString)));
        Date startDate = Date.from(startInstant);
        Instant endInstant = Instant.parse(df.format(new Date(endDateString)));
        Date endDate = Date.from(endInstant);

        List<Data> dataList = new ArrayList<>();
        List<Long> idList = new ArrayList<>();

//        Block<Document> printBlock = document -> dataList.add(setData(document));
        Block<Document> printBlock = document -> {
            idList.add(Long.parseLong(document.get("orderId").toString()));
        };

        List<Bson> aggregatesList = new ArrayList<>(Arrays.asList(
                Aggregates.match(
                        Filters.gt("creationDate", startDate)),
                Aggregates.match(
                        Filters.lt("creationDate", endDate)),
                Aggregates.match(Filters.eq("store.id", storeId)),
                Aggregates.group("$id",
                        Accumulators.first("orderId", "$id"))
        ));

        collection.aggregate(aggregatesList).forEach(printBlock);

        for (Long id:
                idList) {
            collection.deleteMany(Filters.eq("id", id));
            System.out.println("Order Id: " + id + " is deleted");
        }

        return idList;
    }

    public List<Long> getStoresFromDatabase() {
        collection = mongoDatabase.getCollection("Store");
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().toZoneId());
        DateFormat df = new SimpleDateFormat(pattern); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);

        List<Long> idList = new ArrayList<>();
        Block<Document> printBlock = document -> {
            idList.add(Long.parseLong(document.get("storeId").toString()));
        };

        List<Bson> aggregatesList = new ArrayList<>(Arrays.asList(
                Aggregates.group("$id",
                        Accumulators.first("storeId", "$id"))
        ));

        collection.aggregate(aggregatesList).forEach(printBlock);
        collection = mongoDatabase.getCollection("Order");

        Collections.sort(idList);
        return idList;
    }

    public List<Data> getDataFromDatabase(String startDateString, String endDateString, Boolean isConvenienceFeeOnly) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().toZoneId());
        DateFormat df = new SimpleDateFormat(pattern); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);

        Instant startInstant = Instant.parse(df.format(new Date(startDateString)));
        Date startDate = Date.from(startInstant);
        Instant endInstant = Instant.parse(df.format(new Date(endDateString)));
        Date endDate = Date.from(endInstant);

        List<Data> dataList = new ArrayList<>();
        Block<Document> printBlock = document -> dataList.add(setData(document));

        List<Bson> aggregatesList = new ArrayList<>(Arrays.asList(
                Aggregates.match(
                        Filters.gt("creationDate", startDate)),
                Aggregates.match(
                        Filters.lt("creationDate", endDate)),
                Aggregates.match(
                        Filters.in("status", requestOrderStatus)),
                Aggregates.group("$store.id",
                        Accumulators.first("Store ID", "$store.id"),
                        Accumulators.first("Store Name", "$store.name"),
                        Accumulators.sum("Num of Order", 1),
                        Accumulators.sum("ConvenienceFee Total", "$convenienceFee"),
                        Accumulators.sum("Order Total", "$total"))
        ));

        if(isConvenienceFeeOnly) {
            aggregatesList.add(0, Aggregates.match(
                    Filters.gt("convenienceFee", 0)));
        }

        collection.aggregate(aggregatesList).forEach(printBlock);

        return dataList;
    }

    public Data setData(Document document) {
        Data data = new Data();
        data.setStoreId((Long)document.get("Store ID"));
        data.setStoreName(String.valueOf(document.get("Store Name")));
        data.setNumOfOrder((Integer) document.get("Num of Order"));
        data.setConvenienceFeeTotal(new BigDecimal(document.get("ConvenienceFee Total").toString()).setScale(2, RoundingMode.HALF_UP));
        data.setOrderTotal(new BigDecimal(document.get("Order Total").toString()).setScale(2, RoundingMode.HALF_UP));
        mongoLogger.info("Data: " + document);
        return data;
    }
}
