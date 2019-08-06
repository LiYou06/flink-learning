package com.test.flink.stream.task;

import redis.clients.jedis.Jedis;

public class RedisTest {
    public static void main(String args[]){
        Jedis jedis=new Jedis("192.168.1.26");
        System.out.println("Server is running: " + jedis.ping());
        System.out.println("result:"+jedis.hgetAll("flink"));
//        System.out.println("result:"+jedis.hgetAll("gg"));
    }
}
