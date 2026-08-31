package com.example

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundManager {
    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 85)
    } catch (e: Exception) {
        null
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    fun playCoin() {
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_C, 70)
                delay(80)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 100)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playShield() {
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_1, 60)
                delay(70)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_5, 60)
                delay(70)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_9, 120)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playBoost() {
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 80)
                delay(90)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_B, 80)
                delay(90)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_C, 250)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playCrash() {
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun playMenuClick() {
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
