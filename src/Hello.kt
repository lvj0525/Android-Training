import kotlin.math.floor

fun main() {
    val receipt = getReceipt()
    println(receipt)
    val result = when (receipt == expectedReceipt) {
        true -> "正确 ✅"
        false -> "错误 ❌"
    }
    println("\n结果：${result}")
}

interface Promotion {
    var barcodes: List<String>
}

data class BuyTwoGetOneFreePromotion(override var barcodes: List<String>) : Promotion {

}

fun loadPromotions(): List<Promotion> = listOf(BuyTwoGetOneFreePromotion(listOf("ITEM000000", "ITEM000001", "ITEM000005")))

data class Item(val barcode: String, val name: String, val unit: String, val price: Double) {

}

fun loadAllItems(): List<Item> {
    return listOf(
        Item("ITEM000000", "可口可乐", "瓶", 3.00),
        Item("ITEM000001", "雪碧", "瓶", 3.00),
        Item("ITEM000002", "苹果", "斤", 5.50),
        Item("ITEM000003", "荔枝", "斤", 15.00),
        Item("ITEM000004", "电池", "个", 2.00),
        Item("ITEM000005", "方便面", "袋", 4.50)
    )
}

val purchasedBarcodes = listOf(
    "ITEM000001",
    "ITEM000001",
    "ITEM000001",
    "ITEM000001",
    "ITEM000001",
    "ITEM000003-2",
    "ITEM000005",
    "ITEM000005",
    "ITEM000005"
)

fun getFlatPurchasedBarcodes(purchasedBarcodes: List<String>): List<String> {
    return purchasedBarcodes.flatMap { barcode ->
        if (barcode.indexOf("-") > -1) {
            val (code ,count) = barcode.split("-")
            Array(count.toInt()) {code}.toList()
        } else {
            Array(1) {barcode}.toList()
        }
    }
}

fun getSubtotal(item: Item, count: Int): Double {
    val promotionCodes = loadPromotions()[0].barcodes;
    val subtotal = item.price * count;
    if (promotionCodes.indexOf(item.barcode) > -1) {
        return if (count % 3 == 0) {
            subtotal - subtotal / 3
        } else {
            ((count / 3)*2 + count % 3) * item.price
        }
    }
    return subtotal;
}

fun getTotal(flatPurchaseCodes: List<String>, allItems: List<Item>): Double {
    return flatPurchaseCodes.map { code -> allItems.find { it.barcode == code }?.price ?: 0.00}.reduce { acc, d -> acc + d }
}

fun getSavingTotal(flatPurchaseCodes: List<String>, allItems: List<Item>): Double {
    return flatPurchaseCodes.distinct().map { code ->
        val item = allItems.find { it.barcode == code }
        val count = flatPurchaseCodes.count { barcode -> code == barcode }
        getSubtotal(item!!, count)
    }.reduce { acc, d -> acc + d }
}

fun formatDoubleToString(price: Double? = 0.00): String {
    return String.format("%.2f", price)
}

fun getContent(flatPurchaseCodes: List<String>, allItems: List<Item>): String {
    val results = flatPurchaseCodes.distinct().map { code ->
        val item = allItems.find { it.barcode == code }
        val count = flatPurchaseCodes.count { it == code }
        "名称：${item?.name}，数量：${count}${item?.unit}，单价：${formatDoubleToString(item?.price)}(元)，小计：${item?.let { getSubtotal(it, count) }}(元)"
    }
    return results.joinToString(separator = "\n")
}

fun getReceipt(): String {
    val allItems = loadAllItems()
    val flatPurchaseCodes = getFlatPurchasedBarcodes(purchasedBarcodes)
    val total = getTotal(flatPurchaseCodes, allItems)
    val savingTotal = getSavingTotal(flatPurchaseCodes, allItems)
    return """
***<没钱赚商店>收据***
${getContent(flatPurchaseCodes, allItems)}
----------------------
总计：${formatDoubleToString(savingTotal)}(元)
节省：${formatDoubleToString(total - savingTotal)}(元)
**********************
"""
}

const val expectedReceipt = """
***<没钱赚商店>收据***
名称：雪碧，数量：5瓶，单价：3.00(元)，小计：12.0(元)
名称：荔枝，数量：2斤，单价：15.00(元)，小计：30.0(元)
名称：方便面，数量：3袋，单价：4.50(元)，小计：9.0(元)
----------------------
总计：51.00(元)
节省：7.50(元)
**********************
"""
