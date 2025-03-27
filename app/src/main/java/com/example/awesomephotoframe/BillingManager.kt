package com.example.awesomephotoframe

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams


class BillingManager(context: Context) {
    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            // 購入完了処理
            handlePurchase(purchases)
        }
        .enablePendingPurchases()
        .build()

    fun startConnection(onConnected: () -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    onConnected()
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    fun queryPurchases(onResult: (Boolean) -> Unit) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchasesList ->
            val isPremium = purchasesList.any { purchase ->
                purchase.products.contains("premium_upgrade")
            }
            onResult(isPremium)
        }
    }

    private fun handlePurchase(purchases: List<Purchase>?) {
        // 購入済み情報の保存など（SharedPreferences など）
    }
}
