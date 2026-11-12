package com.keepfocus.app.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keepfocus.app.R
import com.keepfocus.app.data.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import javax.inject.Inject
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun KeepFocusScreen() {
    val context = LocalContext.current
    val entry = remember { EntryPointAccessors.fromApplication(context, UiEntryPoint::class.java) }
    val prefs = remember { entry.preferences() }
    val dao = remember { entry.statsDao() }
    val profile by prefs.profile.collectAsStateWithLifecycle(initialValue=Profile())
    val settings by prefs.settings.collectAsStateWithLifecycle(initialValue=FocusSettings())
    val stats by dao.lastWeek().collectAsStateWithLifecycle(initialValue= emptyList())
    val scope = rememberCoroutineScope()
    var screen by remember(settings.setup) { mutableStateOf(if (settings.setup) "home" else "welcome") }
    var fullName by remember { mutableStateOf(profile.name) }; var nick by remember { mutableStateOf(profile.nickname) }
    var dream by remember { mutableStateOf(profile.dream) }; var age by remember { mutableStateOf(if(profile.age==0) "" else profile.age.toString()) }
    var profession by remember { mutableStateOf(profile.profession) }; var line by remember { mutableStateOf(profile.dreamLine) }
    var photo by remember { mutableStateOf(profile.photo) }; var error by remember { mutableStateOf("") }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) try { val target=File(context.filesDir,"profile_${System.currentTimeMillis()}.jpg"); context.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(target).use { input.copyTo(it) } }; photo=target.absolutePath } catch (_: Exception) { error="Could not save selected photo" }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        when(screen) {
            "welcome" -> { Spacer(Modifier.height(70.dp)); Text(stringResource(R.string.welcome_title), fontSize=32.sp, fontWeight=FontWeight.Bold, color=Color(0xFF1E40AF)); Text(stringResource(R.string.welcome_body)); Button(onClick={screen="profile"}, modifier=Modifier.fillMaxWidth()) { Text(stringResource(R.string.continue_text)) } }
            "profile" -> {
                Text(stringResource(R.string.profile_title),fontSize=26.sp,fontWeight=FontWeight.Bold)
                Field(stringResource(R.string.full_name),fullName,{fullName=it}); Field(stringResource(R.string.nickname),nick,{nick=it})
                Field(stringResource(R.string.life_goal),dream,{dream=it}, singleLine=false)
                OutlinedTextField(age,{age=it.filter(Char::isDigit)},label={Text(stringResource(R.string.age))},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth())
                Field(stringResource(R.string.profession),profession,{profession=it})
                Field(stringResource(R.string.dream_line),line,{line=it},singleLine=false)
                OutlinedButton(onClick={photoPicker.launch(ActivityResultContracts.PickVisualMedia.ImageOnly)},modifier=Modifier.fillMaxWidth()) { Text(stringResource(R.string.choose_photo)) }
                if(error.isNotBlank()) Text(error,color=MaterialTheme.colorScheme.error)
                Button(onClick={ if(fullName.isBlank()||dream.isBlank()||age.toIntOrNull()==null) error="Name, dream and valid age are required" else if(age.toInt() !in 10..100) error="Age must be between 10 and 100" else { scope.launch { prefs.saveProfile(Profile(fullName,nick.ifBlank{fullName},dream,line.ifBlank{"${nick.ifBlank{fullName}}, I will make my dream a reality."},age.toInt(),profession,photo) ) }; screen="consent" } },modifier=Modifier.fillMaxWidth()) { Text(stringResource(R.string.continue_text)) }
            }
            "consent" -> {
                Text(stringResource(R.string.consent_title),fontSize=26.sp,fontWeight=FontWeight.Bold)
                Text(stringResource(R.string.consent_body)); Text(stringResource(R.string.overlay_explain))
                Button(onClick={ context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); screen="done" },modifier=Modifier.fillMaxWidth()) { Text(stringResource(R.string.agree)) }
                OutlinedButton(onClick={screen="done"},modifier=Modifier.fillMaxWidth()) { Text(stringResource(R.string.deny)) }
            }
            "done" -> { Text(stringResource(R.string.done),fontSize=28.sp,fontWeight=FontWeight.Bold); Button(onClick={screen="home"}) { Text(stringResource(R.string.home)) } }
            "settings" -> {
                Text(stringResource(R.string.settings),fontSize=28.sp,fontWeight=FontWeight.Bold)
                Text(stringResource(R.string.threshold))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf(10,20,30,50).forEach { n -> FilterChip(selected=settings.threshold==n,onClick={scope.launch{prefs.updateSettings(settings.copy(threshold=n))}},label={Text(n.toString())}) } }
                Text(stringResource(R.string.tracked_apps))
                Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(settings.instagram,{v->scope.launch{prefs.updateSettings(settings.copy(instagram=v))}}); Text("Instagram") }
                Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(settings.youtube,{v->scope.launch{prefs.updateSettings(settings.copy(youtube=v))}}); Text("YouTube") }
                Button(onClick={screen="profile"}) { Text("Edit profile") }
                Button(onClick={screen="home"}) { Text(stringResource(R.string.home)) }
            }
            else -> {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) { Text("Keep Focus",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Color(0xFF1E40AF)); TextButton(onClick={screen="settings"}) { Text(stringResource(R.string.settings)) } }
                Card(shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text(stringResource(R.string.protection)); Row(verticalAlignment=Alignment.CenterVertically) { Text(if(settings.enabled) stringResource(R.string.on) else stringResource(R.string.off),fontSize=26.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.weight(1f)); Switch(settings.enabled,{v->scope.launch{prefs.updateSettings(settings.copy(enabled=v))}}) } } }
                val today=stats.firstOrNull { it.day==LocalDate.now().toString() }
                StatCard(stringResource(R.string.today_count),today?.reels ?: 0)
                StatCard(stringResource(R.string.week_count),stats.sumOf{it.reels})
                StatCard(stringResource(R.string.reminders),today?.reminders ?: 0)
                StatCard(stringResource(R.string.closed_apps),today?.closed ?: 0)
                Text("Weekly activity",fontWeight=FontWeight.SemiBold)
                Canvas(Modifier.fillMaxWidth().height(110.dp)) { val maxValue=(stats.maxOfOrNull{it.reels}?:1).coerceAtLeast(1); val barWidth=size.width/9f; stats.reversed().forEachIndexed { i,s -> val h=size.height*s.reels/maxValue.toFloat(); drawRoundRect(Color(0xFF14B8A6),topLeft=androidx.compose.ui.geometry.Offset(i*barWidth*1.25f,size.height-h),size=androidx.compose.ui.geometry.Size(barWidth,h),cornerRadius=androidx.compose.ui.geometry.CornerRadius(8f)) } }
                val accessibilityEnabled = runCatching { Settings.Secure.getInt(context.contentResolver,Settings.Secure.ACCESSIBILITY_ENABLED)==1 }.getOrDefault(false)
                val overlayEnabled = Settings.canDrawOverlays(context)
                Text("Accessibility: ${if(accessibilityEnabled) "Enabled" else "Not enabled"} · Overlay: ${if(overlayEnabled) "Allowed" else "Not allowed"}")
                Button(onClick={context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))},modifier=Modifier.fillMaxWidth()) { Text(stringResource(R.string.fix_permissions)) }
                OutlinedButton(onClick={context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:${context.packageName}")))},modifier=Modifier.fillMaxWidth()) { Text("Overlay permission") }
            }
        }
    }
}

@Composable private fun Field(label:String,value:String,onValue:(String)->Unit,singleLine:Boolean=true) { OutlinedTextField(value,onValue,label={Text(label)},singleLine=singleLine,modifier=Modifier.fillMaxWidth()) }
@Composable private fun StatCard(label:String,value:Int) { Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp)) { Text(label); Text(value.toString(),fontSize=28.sp,fontWeight=FontWeight.Bold,color=Color(0xFF1E40AF)) } } }

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface UiEntryPoint { fun preferences(): Preferences; fun statsDao(): StatsDao }
