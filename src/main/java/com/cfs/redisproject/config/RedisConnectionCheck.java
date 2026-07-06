package com.cfs.redisproject.config;

import com.cfs.redisproject.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisConnectionCheck {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConnectionCheck(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public void checkConnection()
    {
        log.info("=====================================");
        log.info("Checking Redis connection...");
        try {
            String pong = redisConnectionFactory.getConnection().ping();
            if ("PONG".equals(pong)) {
                log.info("Redis Connection Successfully");
            }
        }
            catch(Exception e)
            {
                log.error("Redis Connection Failed");
            }
    }
}
