<template>
  <div class="seckill-container">
    <h1>秒杀系统</h1>
    <div class="product-card">
      <h2>{{ product.name }}</h2>
      <p class="price">秒杀价: ¥{{ product.price }}</p>
      <p class="stock">剩余库存: {{ stock }}</p>
      <div class="countdown" v-if="!isEnd">
        距离结束: {{ countdown }}
      </div>
      <div class="status" v-else>
        活动已结束
      </div>
      <button 
        class="seckill-btn" 
        :disabled="!canSeckill || isLoading"
        @click="handleSeckill"
      >
        {{ buttonText }}
      </button>
    </div>
    <div class="result" v-if="showResult">
      <p>{{ resultMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useSeckillStore } from './store/seckill'
import axios from 'axios'

const store = useSeckillStore()

const product = ref({
  id: 1,
  name: 'iPhone 15 Pro',
  price: 5999,
  stock: 1000
})

const stock = ref(1000)
const isLoading = ref(false)
const showResult = ref(false)
const resultMessage = ref('')
const countdown = ref('')
const timer = ref<number | null>(null)

const isEnd = computed(() => {
  const endTime = new Date()
  endTime.setMinutes(endTime.getMinutes() + 30)
  return new Date() > endTime
})

const canSeckill = computed(() => {
  return !isEnd.value && stock.value > 0
})

const buttonText = computed(() => {
  if (isLoading.value) return '抢购中...'
  if (!canSeckill.value) return '已售罄'
  return '立即抢购'
})

const updateCountdown = () => {
  const endTime = new Date()
  endTime.setMinutes(endTime.getMinutes() + 30)
  const now = new Date()
  const diff = endTime.getTime() - now.getTime()
  
  if (diff <= 0) {
    countdown.value = '00:00:00'
    return
  }
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  countdown.value = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

const handleSeckill = async () => {
  if (!canSeckill.value || isLoading.value) return
  
  isLoading.value = true
  showResult.value = false
  
  try {
    const response = await axios.post(`/api/seckill/execute/${product.value.id}`)
   // const response = await axios.post(`/api/seckill1233/execute/${product.value.id}`)
    resultMessage.value = response.data
    showResult.value = true
  } catch (error) {
    resultMessage.value = '抢购失败，请稍后重试'
    showResult.value = true
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  // 模拟从Redis获取库存
  stock.value = 1000
  
  // 启动倒计时
  updateCountdown()
  timer.value = window.setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})
</script>

<style scoped>
.seckill-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  text-align: center;
}

.product-card {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}

.price {
  font-size: 24px;
  color: #ff4d4f;
  font-weight: bold;
}

.stock {
  font-size: 16px;
  color: #666;
}

.countdown {
  font-size: 18px;
  color: #1890ff;
  margin: 10px 0;
}

.status {
  font-size: 18px;
  color: #999;
  margin: 10px 0;
}

.seckill-btn {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 20px;
}

.seckill-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.result {
  margin-top: 20px;
  padding: 10px;
  background: #e6f7ff;
  border-radius: 4px;
}
</style>