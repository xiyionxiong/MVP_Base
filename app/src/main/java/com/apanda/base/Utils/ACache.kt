package com.apanda.base.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

import org.json.JSONArray
import org.json.JSONObject

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.RandomAccessFile
import java.io.Serializable
import java.util.Collections
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 本地文件缓存工具类
 */

/********************使用例子 */
/*
支持对象、数组、json、字符串（对象要序列化Serializable）
ACache.get(AppApplication.getAppContext()).put(AppConstant.CHANNEL_MINE,mineChannelTableList);
ACache.get(AppApplication.getAppContext()).get(AppConstant.CHANNEL_MINE);
*/
class ACache private constructor(cacheDir: File, max_size: Long, max_count: Int) {
    private val mCache: ACacheManager

    init {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw RuntimeException("can't make dirs in " + cacheDir.absolutePath)
        }
        mCache = ACacheManager(cacheDir, max_size, max_count)
    }

    // =======================================
    // ============ String数据 读写 ==============
    // =======================================

    /**
     * 保存 String数据 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的String数据
     */
    fun put(key: String, value: String) {
        val file = mCache.newFile(key)
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(FileWriter(file), 1024)
            out.write(value)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            mCache.put(file)
        }
    }

    /**
     * 保存 String数据 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的String数据
     * *
     * @param saveTime
     * *            保存的时间，单位：秒
     */
    fun put(key: String, value: String, saveTime: Int) {
        put(key, Utils.newStringWithDateInfo(saveTime, value))
    }

    /**
     * 读取 String数据

     * @param key
     * *
     * @return String 数据
     */
    fun getAsString(key: String): String? {
        val file = mCache.get(key)
        if (!file.exists())
            return null
        var removeFile = false
        var `in`: BufferedReader? = null
        try {
            `in` = BufferedReader(FileReader(file))
            var readString = ""
            var currentLine: String
            while ((currentLine = `in`.readLine()) != null) {
                readString += currentLine
            }
            if (!Utils.isDue(readString)) {
                return Utils.clearDateInfo(readString)
            } else {
                removeFile = true
                return null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (removeFile)
                remove(key)
        }
    }

    // =======================================
    // ============= JSONObject 数据 读写 ==============
    // =======================================

    /**
     * 保存 JSONObject数据 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的JSON数据
     */
    fun put(key: String, value: JSONObject) {
        put(key, value.toString())
    }

    /**
     * 保存 JSONObject数据 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的JSONObject数据
     * *
     * @param saveTime
     * *            保存的时间，单位：秒
     */
    fun put(key: String, value: JSONObject, saveTime: Int) {
        put(key, value.toString(), saveTime)
    }

    /**
     * 读取JSONObject数据

     * @param key
     * *
     * @return JSONObject数据
     */
    fun getAsJSONObject(key: String): JSONObject? {
        val JSONString = getAsString(key)
        try {
            val obj = JSONObject(JSONString)
            return obj
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    // =======================================
    // ============ JSONArray 数据 读写 =============
    // =======================================

    /**
     * 保存 JSONArray数据 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的JSONArray数据
     */
    fun put(key: String, value: JSONArray) {
        put(key, value.toString())
    }

    /**
     * 保存 JSONArray数据 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的JSONArray数据
     * *
     * @param saveTime
     * *            保存的时间，单位：秒
     */
    fun put(key: String, value: JSONArray, saveTime: Int) {
        put(key, value.toString(), saveTime)
    }

    /**
     * 读取JSONArray数据

     * @param key
     * *
     * @return JSONArray数据
     */
    fun getAsJSONArray(key: String): JSONArray? {
        val JSONString = getAsString(key)
        try {
            val obj = JSONArray(JSONString)
            return obj
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    // =======================================
    // ============== byte 数据 读写 =============
    // =======================================

    /**
     * 保存 byte数据 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的数据
     */
    fun put(key: String, value: ByteArray) {
        val file = mCache.newFile(key)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            out.write(value)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            mCache.put(file)
        }
    }

    /**
     * 保存 byte数据 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的数据
     * *
     * @param saveTime
     * *            保存的时间，单位：秒
     */
    fun put(key: String, value: ByteArray, saveTime: Int) {
        put(key, Utils.newByteArrayWithDateInfo(saveTime, value))
    }

    /**
     * 获取 byte 数据

     * @param key
     * *
     * @return byte 数据
     */
    fun getAsBinary(key: String): ByteArray? {
        var RAFile: RandomAccessFile? = null
        var removeFile = false
        try {
            val file = mCache.get(key)
            if (!file.exists())
                return null
            RAFile = RandomAccessFile(file, "r")
            val byteArray = ByteArray(RAFile.length().toInt())
            RAFile.read(byteArray)
            if (!Utils.isDue(byteArray)) {
                return Utils.clearDateInfo(byteArray)
            } else {
                removeFile = true
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            if (RAFile != null) {
                try {
                    RAFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (removeFile)
                remove(key)
        }
    }

    /**
     * 保存 Serializable数据到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的value
     * *
     * @param saveTime
     * *            保存的时间，单位：秒
     */
    @JvmOverloads fun put(key: String, value: Serializable, saveTime: Int = -1) {
        var baos: ByteArrayOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            oos = ObjectOutputStream(baos)
            oos.writeObject(value)
            val data = baos.toByteArray()
            if (saveTime != -1) {
                put(key, data, saveTime)
            } else {
                put(key, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                oos!!.close()
            } catch (e: IOException) {
            }

        }
    }

    /**
     * 读取 Serializable数据

     * @param key
     * *
     * @return Serializable 数据
     */
    fun getAsObject(key: String): Any? {
        val data = getAsBinary(key)
        if (data != null) {
            var bais: ByteArrayInputStream? = null
            var ois: ObjectInputStream? = null
            try {
                bais = ByteArrayInputStream(data)
                ois = ObjectInputStream(bais)
                val reObject = ois.readObject()
                return reObject
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            } finally {
                try {
                    if (bais != null)
                        bais.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                try {
                    if (ois != null)
                        ois.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return null

    }

    // =======================================
    // ============== bitmap 数据 读写 =============
    // =======================================

    /**
     * 保存 bitmap 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的bitmap数据
     */
    fun put(key: String, value: Bitmap) {
        put(key, Utils.Bitmap2Bytes(value))
    }

    /**
     * 保存 bitmap 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的 bitmap 数据
     * *
     * @param saveTime
     * *            保存的时间，单位：秒
     */
    fun put(key: String, value: Bitmap, saveTime: Int) {
        put(key, Utils.Bitmap2Bytes(value), saveTime)
    }

    /**
     * 读取 bitmap 数据

     * @param key
     * *
     * @return bitmap 数据
     */
    fun getAsBitmap(key: String): Bitmap? {
        if (getAsBinary(key) == null) {
            return null
        }
        return Utils.Bytes2Bimap(getAsBinary(key))
    }

    // =======================================
    // ============= drawable 数据 读写 =============
    // =======================================

    /**
     * 保存 drawable 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的drawable数据
     */
    fun put(key: String, value: Drawable) {
        put(key, Utils.drawable2Bitmap(value))
    }

    /**
     * 保存 drawable 到 缓存中

     * @param key
     * *            保存的key
     * *
     * @param value
     * *            保存的 drawable 数据
     * *
     * @param saveTime
     * *            保存的时间，单位：秒
     */
    fun put(key: String, value: Drawable, saveTime: Int) {
        put(key, Utils.drawable2Bitmap(value), saveTime)
    }

    /**
     * 读取 Drawable 数据

     * @param key
     * *
     * @return Drawable 数据
     */
    fun getAsDrawable(key: String): Drawable? {
        if (getAsBinary(key) == null) {
            return null
        }
        return Utils.bitmap2Drawable(Utils.Bytes2Bimap(getAsBinary(key)))
    }

    /**
     * 获取缓存文件

     * @param key
     * *
     * @return value 缓存的文件
     */
    fun file(key: String): File? {
        val f = mCache.newFile(key)
        if (f.exists())
            return f
        return null
    }

    /**
     * 移除某个key

     * @param key
     * *
     * @return 是否移除成功
     */
    fun remove(key: String): Boolean {
        return mCache.remove(key)
    }

    /**
     * 清除所有数据
     */
    fun clear() {
        mCache.clear()
    }

    /**
     * @title 缓存管理器
     * *
     * @author 杨福海（michael） www.yangfuhai.com
     * *
     * @version 1.0
     */
    inner class ACacheManager private constructor(protected var cacheDir: File, private val sizeLimit: Long, private val countLimit: Int) {
        private val cacheSize: AtomicLong
        private val cacheCount: AtomicInteger
        private val lastUsageDates = Collections.synchronizedMap(HashMap<File, Long>())

        init {
            cacheSize = AtomicLong()
            cacheCount = AtomicInteger()
            calculateCacheSizeAndCacheCount()
        }

        /**
         * 计算 cacheSize和cacheCount
         */
        private fun calculateCacheSizeAndCacheCount() {
            Thread(Runnable {
                var size = 0
                var count = 0
                val cachedFiles = cacheDir.listFiles()
                if (cachedFiles != null) {
                    for (cachedFile in cachedFiles) {
                        size += calculateSize(cachedFile).toInt()
                        count += 1
                        lastUsageDates.put(cachedFile,
                                cachedFile.lastModified())
                    }
                    cacheSize.set(size.toLong())
                    cacheCount.set(count)
                }
            }).start()
        }

        private fun put(file: File) {
            var curCacheCount = cacheCount.get()
            while (curCacheCount + 1 > countLimit) {
                val freedSize = removeNext()
                cacheSize.addAndGet(-freedSize)

                curCacheCount = cacheCount.addAndGet(-1)
            }
            cacheCount.addAndGet(1)

            val valueSize = calculateSize(file)
            var curCacheSize = cacheSize.get()
            while (curCacheSize + valueSize > sizeLimit) {
                val freedSize = removeNext()
                curCacheSize = cacheSize.addAndGet(-freedSize)
            }
            cacheSize.addAndGet(valueSize)

            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            lastUsageDates.put(file, currentTime)
        }

        private operator fun get(key: String): File {
            val file = newFile(key)
            val currentTime = System.currentTimeMillis()
            file.setLastModified(currentTime)
            lastUsageDates.put(file, currentTime)

            return file
        }

        private fun newFile(key: String): File {
            return File(cacheDir, key.hashCode() + "")
        }

        private fun remove(key: String): Boolean {
            val image = get(key)
            return image.delete()
        }

        private fun clear() {
            lastUsageDates.clear()
            cacheSize.set(0)
            val files = cacheDir.listFiles()
            if (files != null) {
                for (f in files) {
                    f.delete()
                }
            }
        }

        /**
         * 移除旧的文件

         * @return
         */
        private fun removeNext(): Long {
            if (lastUsageDates.isEmpty()) {
                return 0
            }

            var oldestUsage: Long? = null
            var mostLongUsedFile: File? = null
            val entries = lastUsageDates.entries
            synchronized(lastUsageDates) {
                for ((key, lastValueUsage) in entries) {
                    if (mostLongUsedFile == null) {
                        mostLongUsedFile = key
                        oldestUsage = lastValueUsage
                    } else {
                        if (lastValueUsage < oldestUsage) {
                            oldestUsage = lastValueUsage
                            mostLongUsedFile = key
                        }
                    }
                }
            }

            val fileSize = calculateSize(mostLongUsedFile)
            if (mostLongUsedFile!!.delete()) {
                lastUsageDates.remove(mostLongUsedFile)
            }
            return fileSize
        }

        private fun calculateSize(file: File): Long {
            return file.length()
        }
    }

    /**
     * @title 时间计算工具类
     * *
     * @author 杨福海（michael） www.yangfuhai.com
     * *
     * @version 1.0
     */
    private object Utils {

        /**
         * 判断缓存的String数据是否到期

         * @param str
         * *
         * @return true：到期了 false：还没有到期
         */
        private fun isDue(str: String): Boolean {
            return isDue(str.toByteArray())
        }

        /**
         * 判断缓存的byte数据是否到期

         * @param data
         * *
         * @return true：到期了 false：还没有到期
         */
        private fun isDue(data: ByteArray): Boolean {
            val strs = getDateInfoFromDate(data)
            if (strs != null && strs.size == 2) {
                var saveTimeStr = strs[0]
                while (saveTimeStr.startsWith("0")) {
                    saveTimeStr = saveTimeStr.substring(1, saveTimeStr.length)
                }
                val saveTime = java.lang.Long.valueOf(saveTimeStr)!!
                val deleteAfter = java.lang.Long.valueOf(strs[1])!!
                if (System.currentTimeMillis() > saveTime + deleteAfter * 1000) {
                    return true
                }
            }
            return false
        }

        private fun newStringWithDateInfo(second: Int, strInfo: String): String {
            return createDateInfo(second) + strInfo
        }

        private fun newByteArrayWithDateInfo(second: Int, data2: ByteArray): ByteArray {
            val data1 = createDateInfo(second).toByteArray()
            val retdata = ByteArray(data1.size + data2.size)
            System.arraycopy(data1, 0, retdata, 0, data1.size)
            System.arraycopy(data2, 0, retdata, data1.size, data2.size)
            return retdata
        }

        private fun clearDateInfo(strInfo: String?): String {
            var strInfo = strInfo
            if (strInfo != null && hasDateInfo(strInfo.toByteArray())) {
                strInfo = strInfo.substring(strInfo.indexOf(mSeparator.toInt()) + 1,
                        strInfo.length)
            }
            return strInfo
        }

        private fun clearDateInfo(data: ByteArray): ByteArray {
            if (hasDateInfo(data)) {
                return copyOfRange(data, indexOf(data, mSeparator) + 1,
                        data.size)
            }
            return data
        }

        private fun hasDateInfo(data: ByteArray?): Boolean {
            return data != null && data.size > 15 && data[13] == '-'
                    && indexOf(data, mSeparator) > 14
        }

        private fun getDateInfoFromDate(data: ByteArray): Array<String>? {
            if (hasDateInfo(data)) {
                val saveDate = String(copyOfRange(data, 0, 13))
                val deleteAfter = String(copyOfRange(data, 14,
                        indexOf(data, mSeparator)))
                return arrayOf(saveDate, deleteAfter)
            }
            return null
        }

        private fun indexOf(data: ByteArray, c: Char): Int {
            for (i in data.indices) {
                if (data[i] == c.toByte()) {
                    return i
                }
            }
            return -1
        }

        private fun copyOfRange(original: ByteArray, from: Int, to: Int): ByteArray {
            val newLength = to - from
            if (newLength < 0)
                throw IllegalArgumentException(from + " > " + to)
            val copy = ByteArray(newLength)
            System.arraycopy(original, from, copy, 0,
                    Math.min(original.size - from, newLength))
            return copy
        }

        private val mSeparator = ' '

        private fun createDateInfo(second: Int): String {
            var currentTime = System.currentTimeMillis() + ""
            while (currentTime.length < 13) {
                currentTime = "0" + currentTime
            }
            return currentTime + "-" + second + mSeparator
        }

        /*
         * Bitmap → byte[]
         */
        private fun Bitmap2Bytes(bm: Bitmap?): ByteArray? {
            if (bm == null) {
                return null
            }
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
            return baos.toByteArray()
        }

        /*
         * byte[] → Bitmap
         */
        private fun Bytes2Bimap(b: ByteArray): Bitmap? {
            if (b.size == 0) {
                return null
            }
            return BitmapFactory.decodeByteArray(b, 0, b.size)
        }

        /*
         * Drawable → Bitmap
         */
        private fun drawable2Bitmap(drawable: Drawable?): Bitmap? {
            if (drawable == null) {
                return null
            }
            // 取 drawable 的长宽
            val w = drawable.intrinsicWidth
            val h = drawable.intrinsicHeight
            // 取 drawable 的颜色格式
            val config = if (drawable.opacity != PixelFormat.OPAQUE)
                Bitmap.Config.ARGB_8888
            else
                Bitmap.Config.RGB_565
            // 建立对应 bitmap
            val bitmap = Bitmap.createBitmap(w, h, config)
            // 建立对应 bitmap 的画布
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, w, h)
            // 把 drawable 内容画到画布中
            drawable.draw(canvas)
            return bitmap
        }

        /*
         * Bitmap → Drawable
         */
        @SuppressWarnings("deprecation")
        private fun bitmap2Drawable(bm: Bitmap?): Drawable? {
            if (bm == null) {
                return null
            }
            return BitmapDrawable(bm)
        }
    }

    companion object {

        val TIME_HOUR = 60 * 60
        val TIME_DAY = TIME_HOUR * 24
        private val MAX_SIZE = 1000 * 1000 * 50 // 50 mb
        private val MAX_COUNT = Integer.MAX_VALUE // 不限制存放数据的数量
        private val mInstanceMap = HashMap<String, ACache>()

        @JvmOverloads operator fun get(ctx: Context, cacheName: String = "ACache"): ACache {
            val f = File(ctx.cacheDir, cacheName)
            return get(f, MAX_SIZE.toLong(), MAX_COUNT)
        }

        operator fun get(ctx: Context, max_zise: Long, max_count: Int): ACache {
            val f = File(ctx.cacheDir, "ACache")
            return get(f, max_zise, max_count)
        }

        @JvmOverloads operator fun get(cacheDir: File, max_zise: Long = MAX_SIZE.toLong(), max_count: Int = MAX_COUNT): ACache {
            var manager: ACache? = mInstanceMap[cacheDir.absoluteFile + myPid()]
            if (manager == null) {
                manager = ACache(cacheDir, max_zise, max_count)
                mInstanceMap.put(cacheDir.absolutePath + myPid(), manager)
            }
            return manager
        }

        private fun myPid(): String {
            return "_" + android.os.Process.myPid()
        }
    }

}// =======================================
// ============= 序列化 数据 读写 ===============
// =======================================
/**
 * 保存 Serializable数据 到 缓存中

 * @param key
 * *            保存的key
 * *
 * @param value
 * *            保存的value
 */