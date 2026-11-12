package com.keepfocus.app.data

interface PremiumManager { fun isFeatureAvailable(feature: String): Boolean }
class FreePremiumManager : PremiumManager { override fun isFeatureAvailable(feature: String): Boolean = true }
