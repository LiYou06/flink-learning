package com.test.flink.stream.task;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;

import java.util.Properties;

public class FlinkCostKafka {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(1000);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "192.168.1.26:9092");
        properties.setProperty("zookeeper.connect", "192.168.1.26:2181");
        properties.setProperty("group.id", "test");

        FlinkKafkaConsumer011<String> myConsumer = new FlinkKafkaConsumer011<String>("apache-flink-test", new SimpleStringSchema(), properties);


        // 设置 Flink 读取 Kafka topic 消息的方式
        // 设置 Flink 读取 Kafka topic 最新消息数据
//        consumer.setStartFromLatest();

        // 设置 Flink 读取 Kafka topic 起始消息数据（从头开始消费 Kafka topic 消息）
        myConsumer.setStartFromEarliest();

        DataStream<String> stream = env.addSource(myConsumer);

        System.out.println("测试数据流");
        stream.print();



        DataStream<Tuple2<String, Integer>> counts = stream.flatMap(new LineSplitter()).keyBy(0).sum(1);

        //实例化Flink和Redis关联类FlinkJedisPoolConfig，设置Redis端口
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("192.168.1.26").setPort(6379).build();
        //实例化RedisSink，并通过flink的addSink的方式将flink计算的结果插入到redis
        counts.addSink(new RedisSink<Tuple2<String, Integer>>(conf,new RedisExampleMapper()));
        env.execute("WordCount from Kafka data");
    }

    public static final class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
        private static final long serialVersionUID = 1L;

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // 按空格或换行符分割
//            String[] tokens = value.toLowerCase().split("\\W+");

            // 按换行符分割
            String[] tokens = value.toLowerCase().split("\\n");
            for (String token : tokens) {
                if (token.length() > 0) {
//                    out.collect(new Tuple2<String, Integer>(token, 1));
                    out.collect(new Tuple2<String, Integer>("1", 1));
                }
            }
        }
    }

    //指定Redis key并将flink数据类型映射到Redis数据类型
    public static final class RedisExampleMapper implements RedisMapper<Tuple2<String,Integer>>{
        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.HSET, "flink3");
        }

        @Override
        public String getKeyFromData(Tuple2<String, Integer> data) {
            return data.f0;
        }

        @Override
        public String getValueFromData(Tuple2<String, Integer> data) {
            return data.f1.toString();
        }
    }
}
