package com.bupt.javaEE.Test;

import com.bupt.javaEE.Bean.StockQuotationInfo;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoxingyu on 2018/6/27.
 * Kafka Java API 多线程生成者
 * 没实现，不知道哪里出问题
 */
public class QuotationProducerThread {
    private static final Logger LOG = Logger.getLogger(QuotaitonProducer.class);

    private static final int MSG_SIZE = 100;

    private static final int THREADS_NUMS = 1;

    private static final String TOPIC = "hello_topic";

    private static final String BROKER_LIST = "localhost:9092";

    private static KafkaProducer<String,String> producer = null;

    static {
        // 构造用于实例化KafkaProducer的Properties信息
        Properties configs = initConfig();
        // 初始化一个KafkaProducer
        producer = new KafkaProducer<String,String >(configs);
    }

    /**
     * 初始化Kafka配置
     * @return
     */
    private static Properties initConfig() {
        Properties properties = new Properties();
        // kafka broker列表
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,BROKER_LIST);
        // 设置序列化类
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }


    /**
     * 模拟生产股票行情信息
     * @return
     */
    private static StockQuotationInfo createQuotaitonInfo() {
        StockQuotationInfo quotationInfo = new StockQuotationInfo();

        // 随机产生范围在1-10的随机数，与600100组成股票代码
        Random r = new Random();
        Integer stockCode = 600100 + r.nextInt(10);

        // 随机产生一个0到1之间的浮点数
        float random = (float) Math.random();
        // 设置涨跌规则
        if (random / 2 < 0.5) {
            random = - random;
        }

        DecimalFormat decimalFormat = new DecimalFormat(".00"); // 设置保留两位有效数字
        quotationInfo.setCurrentPrice(Float.valueOf(decimalFormat.format(11 + random))); // 设置最新价格在11元浮动
        quotationInfo.setPreClosePrice(11.50f); // 设置收盘价
        quotationInfo.setOpenPrice(11.75f); // 设置开盘价
        quotationInfo.setLowPrice(11.50f); // 设置最低价
        quotationInfo.setHighPrice(12.50f); // 设置最高价
        quotationInfo.setStockCode(stockCode.toString()); // 设置股票代码
        quotationInfo.setTradeTime(System.currentTimeMillis()); // 设置交易时间
        quotationInfo.setStockName("股票-"+stockCode);
        return quotationInfo;
    }




    public static void main(String[] args) {
        ProducerRecord<String,String> record = null;
        StockQuotationInfo quotationInfo = null;
        ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUMS);
        try {
            for (int i = 0; i < MSG_SIZE; i++) {
                quotationInfo = createQuotaitonInfo();
                record = new ProducerRecord<String, String>(TOPIC,null,quotationInfo.getStockCode(),quotationInfo.toString());
                executor.submit(new KafkaProducerThread(producer,record));
            }
        } catch (Exception e) {
            LOG.error("send message occurs exception",e);
        } finally {
            producer.close();
            executor.shutdown();
        }
    }
}
