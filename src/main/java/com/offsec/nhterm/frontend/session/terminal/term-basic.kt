package com.offsec.nhterm.frontend.session.terminal

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.offsec.nhterm.backend.TerminalSession
import com.offsec.nhterm.frontend.session.view.TerminalView
import com.offsec.nhterm.frontend.session.view.TerminalViewClient

/**
 * A basic [TerminalSession.SessionChangedCallback] implementation that
 * triggers [TerminalView.onScreenUpdated] on text/color changes.
 */
open class BasicSessionCallback(var terminalView: TerminalView) : TerminalSession.SessionChangedCallback {
  override fun onTextChanged(changedSession: TerminalSession?) {
    if (changedSession != null) {
      terminalView.onScreenUpdated()
    }
  }

  override fun onTitleChanged(changedSession: TerminalSession?) {
  }

  override fun onSessionFinished(finishedSession: TerminalSession?) {
  }

  override fun onClipboardText(session: TerminalSession?, text: String?) {
  }

  override fun onBell(session: TerminalSession?) {
  }

  override fun onColorsChanged(session: TerminalSession?) {
    if (session != null) {
      terminalView.onScreenUpdated()
    }
  }
}

/**
 * A basic [TerminalViewClient] implementation with sensible defaults.
 * - Single tap shows soft keyboard
 * - Pinch-to-zoom adjusts text size (steps of 2)
 */
open class BasicViewClient(val terminalView: TerminalView) : TerminalViewClient {

  private var textSize = 14f

  override fun onScale(scale: Float): Float {
    if (scale < 0.9f || scale > 1.1f) {
      val increase = scale > 1f
      val changedSize = (if (increase) 1 else -1) * 2
      val newSize = (terminalView.textSize + changedSize).coerceIn(8, 96)
      terminalView.textSize = newSize
      return 1.0f
    }
    return scale
  }

  override fun onSingleTapUp(e: MotionEvent?) {
    if (terminalView.isFocusable && terminalView.isFocusableInTouchMode) {
      (terminalView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(terminalView, InputMethodManager.SHOW_IMPLICIT)
    }
  }

  override fun shouldBackButtonBeMappedToEscape(): Boolean {
    return false
  }

  override fun copyModeChanged(copyMode: Boolean) {
  }

  override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
    return false
  }

  override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
    return false
  }

  override fun readControlKey(): Boolean {
    return false
  }

  override fun readAltKey(): Boolean {
    return false
  }

  override fun readShiftKey(): Boolean {
    return false
  }

  override fun readFnKey(): Boolean {
    return false
  }

  override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean {
    return false
  }

  override fun onLongPress(event: MotionEvent?): Boolean {
    return false
  }
}
