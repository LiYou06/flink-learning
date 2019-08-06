package com.test.flink.stream.task;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;


/**
 * @author 李优
 * @date 2019-08-06 22:54:00
 * @decription Flink 消费 Kafka 消息 示例类
 */
public class FlinkCostKafkaTask {

	public static void main(String[] args) throws Exception {

		/* *********************  配置1： 设置 Flink stream 执行环境  *********************  */
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();



		/* *********************  配置2： Flink 检查点配置  *********************  */
		// 默认情况下，检查点被禁用。要启用检查点，请在StreamExecutionEnvironment上调用enableCheckpointing(n)方法，
		// 其中n是以毫秒为单位的检查点间隔。每隔5000 ms进行启动一个检查点,则下一个检查点将在上一个检查点完成后5秒钟内启动
		env.enableCheckpointing(500);
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);



		/* ********************* 配置3： 设置 Kafka 配置信息（Zookeeper 和 Kafka IP地址端口号）  *********************  */
		Properties kafkaPro = new Properties();

		// Zookeeper节点IP地址或者hostName，多个使用逗号进行分隔
		kafkaPro.setProperty("zookeeper.connect", "192.168.1.26:2181");

		// Kafka节点IP地址或者hostName，多个使用逗号分隔
		kafkaPro.setProperty("bootstrap.servers", "192.168.1.26:9092");

		// Flink 消费 Kafka 消息，消费者 consumer group.id 配置
		kafkaPro.setProperty("group.id", "test-consumer-group");

		// Flink 消费 Kafka topic 为 "apache-flink-test" 的消息
		FlinkKafkaConsumer011<String> consumer = new FlinkKafkaConsumer011<>("apache-flink-test", new SimpleStringSchema(), kafkaPro);

		// 设置 Flink 读取 Kafka topic 消息的方式
        // 设置 Flink 读取 Kafka topic 最新消息数据
//        consumer.setStartFromLatest();

		// 设置 Flink 读取 Kafka topic 起始消息数据（从头开始消费 Kafka topic 消息）
        consumer.setStartFromEarliest();



		/* ********************* 配置4： 设置 Flink 的数据源为 Kafka topic 生产者消费消息数据  *********************  */
		DataStream<String> sourceStream = env.addSource(consumer);



		/* ********************* 配置5： 对 Fllink 数据源 Kafka topic 消息流数据进行处理  *********************  */
		// 这里，直接将从 Kafka topiic 生产者接收到的消息流数据在控制台上进行打印
		sourceStream.print();


		/* ********************* 配置6： 运行 Flink Stream 任务  *********************  */
		env.execute("Flink Streaming Java API Skeleton");

	}
}
