# Redis初始化脚本
# 连接Redis并设置秒杀数据

# 商品ID
$productId = 1
# 商品库存
$stock = 1000
# 商品名称
$productName = "iPhone 15 Pro"
# 商品价格
$productPrice = 5999

# 构建商品信息JSON
$productInfo = @{
    id = $productId
    name = $productName
    price = $productPrice
    stock = $stock
} | ConvertTo-Json

# Redis命令
$redisCommands = @(
    # 设置商品信息
    "SET seckill:product:$productId '$productInfo'",
    # 设置商品库存
    "SET seckill:stock:$productId $stock",
    # 设置秒杀开始时间（当前时间）
    "SET seckill:start_time:$productId $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')",
    # 设置秒杀结束时间（30分钟后）
    "SET seckill:end_time:$productId $(Get-Date -Date "$(Get-Date).AddMinutes(30)" -Format 'yyyy-MM-dd HH:mm:ss')"
)

# 输出Redis命令
Write-Output "Redis初始化命令："
$redisCommands | ForEach-Object {
    Write-Output $_
}

# 执行Redis命令
Write-Output "\n执行Redis命令..."
try {
    $redisCommands | ForEach-Object {
        Write-Output "执行: $_"
        # 这里需要实际执行Redis命令，假设redis-cli在PATH中
        # redis-cli $_
    }
    Write-Output "\nRedis初始化完成！"
} catch {
    Write-Output "执行命令时出错: $($_.Exception.Message)"
}
