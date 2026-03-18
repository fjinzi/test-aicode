# 高并发秒杀系统

## 项目结构

```
├── frontend/          # 前端Vue 3项目
│   ├── src/           # 前端源码
│   │   ├── store/     # Pinia状态管理
│   │   ├── App.vue    # 主组件
│   │   └── main.ts    # 入口文件
│   ├── package.json   # 前端依赖
│   └── vite.config.ts # Vite配置
├── src/               # 后端Java源码
│   ├── main/java/com/seckill/
│   │   ├── config/    # 配置类
│   │   ├── controller/ # 控制器
│   │   ├── consumer/  # RocketMQ消费者
│   │   ├── dto/       # 数据传输对象
│   │   ├── entity/    # 实体类
│   │   └── SeckillApplication.java # 主应用
│   └── main/resources/
│       └── application.yml # 应用配置
└── pom.xml            # Maven依赖
```

## 技术栈

- 前端：Vue 3 + TypeScript + Axios + Pinia
- 后端：Java 17 (Spring Boot 3) + MyBatis-Plus
- 缓存/中间件：Redis (Redisson) + RocketMQ
- 数据库：MySQL

## 核心功能

1. **防超卖**：使用Redisson分布式锁确保库存扣减的原子性
2. **防重复提交**：基于用户ID和商品ID的锁粒度，实现一人一单
3. **流量削峰**：使用RocketMQ异步处理订单创建
4. **库存预热**：秒杀开始前将库存加载到Redis
5. **兜底保护**：数据库层面使用乐观锁

## 运行步骤

### 1. 环境准备

- JDK 17+
- MySQL 5.7+
- Redis 6.0+
- RocketMQ 4.9+

### 2. 数据库初始化

创建数据库 `seckill` 并执行以下SQL：

```sql
-- 商品表
CREATE TABLE `seckill_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `stock` int(11) NOT NULL,
  `price` double NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `version` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE `seckill_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `price` double NOT NULL,
  `status` int(11) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试数据
INSERT INTO `seckill_product` (`name`, `stock`, `price`, `start_time`, `end_time`) 
VALUES ('iPhone 15 Pro', 1000, 5999, NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE));
```

### 3. 后端启动

1. 配置 `application.yml` 中的数据库连接信息
2. 运行 `SeckillApplication.java` 启动Spring Boot应用

### 4. 前端启动

1. 进入 `frontend` 目录
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 访问 `http://localhost:3000`

### 5. 缓存预热

秒杀开始前，需要将商品库存加载到Redis。可以通过以下方式：

1. 手动执行Redis命令：
   ```bash
   redis-cli> SET seckill:stock:1 1000
   redis-cli> SET seckill:product:1 '{"id":1,"name":"iPhone 15 Pro","price":5999}'
   ```

2. 或者在后端添加定时任务自动预热

## 性能优化

1. **Redis集群**：使用Redis集群提高缓存性能和可用性
2. **RocketMQ集群**：部署RocketMQ集群确保消息处理的高可用性
3. **JVM调优**：合理设置JVM参数，如堆内存、GC策略等
4. **数据库优化**：使用索引、分库分表等策略
5. **CDN加速**：静态资源通过CDN分发

## 注意事项

1. 确保Redis和RocketMQ服务正常运行
2. 生产环境中需要配置合适的连接池大小
3. 定期监控系统性能，及时调整配置
4. 进行充分的压力测试，确保系统能承受预期流量