package com.seckill.controller;

import com.seckill.dto.SeckillRequestDTO;
import com.seckill.entity.SeckillProduct;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class SeckillController {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @PostMapping("/api/seckill/execute/{productId}")
    public String seckill(@PathVariable String productId) {
        // 模拟用户ID
        Long userId = 1L;

        // 检查秒杀时间
        SeckillProduct product = (SeckillProduct) redisTemplate.opsForValue().get("seckill:product:" + productId);

        if (product == null) {
            return "商品不存在";
        }

        // 获取分布式锁
        String lockKey = "seckill:lock:" + productId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁
            boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                return "系统繁忙，请稍后重试";
            }

            // 检查库存
            Integer stock = (Integer) redisTemplate.opsForValue().get("seckill:stock:" + productId);
            if (stock == null || stock <= 0) {
                return "库存不足";
            }

            // 检查是否已购买
            Boolean purchased = redisTemplate.hasKey("seckill:purchased:" + productId + ":" + userId);
            if (Boolean.TRUE.equals(purchased)) {
                return "您已购买过该商品";
            }

            // 扣减库存
            redisTemplate.opsForValue().decrement("seckill:stock:" + productId);

            // 标记已购买
            redisTemplate.opsForValue().set("seckill:purchased:" + productId + ":" + userId, "1");

            // 构建消息
            String message = userId + "," + productId + "," + product.getPrice();

            // 发送消息到RocketMQ
//            rocketMQTemplate.convertAndSend("seckill-topic", message);

            return "排队成功";
        } catch (Exception e) {
            log.error("秒杀失败", e);
            return "系统错误";
        } finally {
            // 释放锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @PostMapping("/api/seckill1233/execute/{productId}")
    public String seckill1(@PathVariable String productId) {
        // 模拟用户ID
        Long userId = 1L;
        SeckillProduct seckillProduct =new SeckillProduct();
        seckillProduct.setId(1L);
        seckillProduct.setPrice(5999D);
        seckillProduct.setName("iphone");
        seckillProduct.setStock(100);
        redisTemplate.opsForValue().set("seckill:product:1",seckillProduct);
        // 检查秒杀时间

        return null;
    }
}