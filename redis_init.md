# Redis初始化指南

## 初始化步骤

### 方法1：使用redis-cli直接执行

1. 打开命令行终端
2. 进入项目目录
3. 执行以下命令：

```bash
# Windows系统
redis-cli -h localhost -p 6379 < init_redis.txt

# Linux/Mac系统
redis-cli < init_redis.txt
```

### 方法2：手动执行Redis命令

连接到Redis后，执行以下命令：

```redis
# 设置商品信息
SET seckill:product:1 '{"id":1,"name":"iPhone 15 Pro","price":5999,"stock":1000}'

# 设置商品库存
SET seckill:stock:1 1000

# 设置秒杀开始时间
SET seckill:start_time:1 "2026-03-17 15:00:00"

# 设置秒杀结束时间
SET seckill:end_time:1 "2026-03-17 15:30:00"
```

## 验证数据

执行以下命令验证数据是否正确：

```redis
# 查看商品信息
GET seckill:product:1

# 查看库存
GET seckill:stock:1

# 查看开始时间
GET seckill:start_time:1

# 查看结束时间
GET seckill:end_time:1
```

## 数据说明

- **seckill:product:{productId}** - 商品详细信息
- **seckill:stock:{productId}** - 商品库存数量
- **seckill:start_time:{productId}** - 秒杀开始时间
- **seckill:end_time:{productId}** - 秒杀结束时间
- **seckill:purchased:{productId}:{userId}** - 用户购买标记（系统自动生成）

## 注意事项

1. 确保Redis服务已启动
2. 确保Redis端口正确（默认6379）
3. 初始化数据应在秒杀开始前完成
4. 库存数量应与数据库中的初始库存一致
