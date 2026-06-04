package com.offsec.nhterm.frontend.session.view.extrakey

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import com.offsec.terminal.lib.R
import com.offsec.nhterm.frontend.session.view.TerminalView

class ExtraKeysView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
  companion object {
    private val ESC = ControlButton(IExtraButton.KEY_ESC)
    private val TAB = ControlButton(IExtraButton.KEY_TAB)
    private val PAGE_UP = RepeatableButton(IExtraButton.KEY_PAGE_UP)
    private val PAGE_DOWN = RepeatableButton(IExtraButton.KEY_PAGE_DOWN)
    private val HOME = ControlButton(IExtraButton.KEY_HOME)
    private val END = ControlButton(IExtraButton.KEY_END)
    private val ARROW_UP = ArrowButton(IExtraButton.KEY_ARROW_UP)
    private val ARROW_DOWN = ArrowButton(IExtraButton.KEY_ARROW_DOWN)
    private val ARROW_LEFT = ArrowButton(IExtraButton.KEY_ARROW_LEFT)
    private val ARROW_RIGHT = ArrowButton(IExtraButton.KEY_ARROW_RIGHT)
    private val SLASH = ControlButton(IExtraButton.KEY_SLASH)

    private val MAX_BUTTONS_PER_LINE = 7
    private const val DEFAULT_ALPHA = 0.8f
    private const val EXPANDED_ALPHA = 0.8f
    private const val USER_KEYS_BUTTON_LINE_START = 3
  }

  private val builtinKeys = mutableListOf<IExtraButton>()
  private val userKeys = mutableListOf<IExtraButton>()

  private val buttonBars: MutableList<LinearLayout> = mutableListOf()
  private var typeface: Typeface? = null

  private val CTRL = StatedControlButton(IExtraButton.KEY_CTRL)
  private val ALT = StatedControlButton(IExtraButton.KEY_ALT)
  private val FN = StatedControlButton(IExtraButton.KEY_FN)
  private val SHIFT = StatedControlButton(IExtraButton.KEY_SHIFT)

  private var buttonPanelExpanded = false

  private val EXPAND_BUTTONS = object : ControlButton(IExtraButton.KEY_SHOW_ALL_BUTTONS) {
    override fun onClick(view: View) {
      expandButtonPanel()
    }
  }

  /** Callback for toggling IME (soft keyboard) */
  var onToggleIme: (() -> Unit)? = null

  private val TOGGLE_IME = object : ControlButton(IExtraButton.KEY_TOGGLE_IME) {
    override fun onClick(view: View) {
      onToggleIme?.invoke()
    }
  }

  init {
    alpha = DEFAULT_ALPHA
    gravity = Gravity.TOP
    orientation = VERTICAL
    typeface = Typeface.DEFAULT

    initBuiltinKeys()
    updateButtons()
    expandButtonPanel(forceSetExpanded = false)
  }

  /** Set the TerminalView to receive text output from this key toolbar */
  fun attachTerminalView(terminalView: TerminalView) {
    IExtraButton.terminalViewProvider = { terminalView }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN) {
      if (buttonPanelExpanded) {
        expandButtonPanel()
        return true
      }
      return false
    }
    return super.onKeyDown(keyCode, event)
  }

  fun setTextColor(textColor: Int) {
    IExtraButton.NORMAL_TEXT_COLOR = textColor
    updateButtons()
  }

  fun setTypeface(typeface: Typeface?) {
    this.typeface = typeface
    updateButtons()
  }

  fun readControlButton(): Boolean {
    return CTRL.readState()
  }

  fun readAltButton(): Boolean {
    return ALT.readState()
  }

  fun readFnButton(): Boolean {
    return FN.readState()
  }

  fun readShiftButton(): Boolean {
    return SHIFT.readState()
  }

  fun addUserKey(button: IExtraButton) {
    addKeyButton(userKeys, button)
  }

  fun addBuiltinKey(button: IExtraButton) {
    addKeyButton(builtinKeys, button)
  }

  fun clearUserKeys() {
    userKeys.clear()
  }

  fun updateButtons() {
    buttonBars.forEach { it.removeAllViews() }

    var targetButtonBarIndex = 0
    builtinKeys.plus(userKeys).forEachIndexed { index, button ->
      addKeyButton(getButtonBarOrNew(targetButtonBarIndex), button)
      targetButtonBarIndex = (index + 1) / MAX_BUTTONS_PER_LINE
    }
    updateButtonBars()
  }

  private fun updateButtonBars() {
    removeAllViews()
    buttonBars.asReversed()
      .forEach { addView(it) }
  }

  private fun expandButtonPanel(forceSetExpanded: Boolean? = null) {
    if (buttonBars.size <= 3) {
      return
    }

    buttonPanelExpanded = forceSetExpanded ?: !buttonPanelExpanded
    val visibility = if (buttonPanelExpanded) View.VISIBLE else View.GONE
    alpha = if (buttonPanelExpanded) EXPANDED_ALPHA else DEFAULT_ALPHA

    IntRange(USER_KEYS_BUTTON_LINE_START, buttonBars.size - 1)
      .map { buttonBars[it] }
      .forEach { it.visibility = visibility }
  }

  private fun createNewButtonBar(): LinearLayout {
    val line = LinearLayout(context)
    val layoutParams = LinearLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT
    )
    layoutParams.setMargins(0, 0, 0, 0)
    line.setPadding(0, 0, 0, 0)
    line.gravity = Gravity.START
    line.orientation = LinearLayout.HORIZONTAL
    line.layoutParams = layoutParams
    return line
  }

  private fun getButtonBarOrNew(position: Int): LinearLayout {
    if (position >= buttonBars.size) {
      for (i in 0..(position - buttonBars.size + 1)) {
        buttonBars.add(createNewButtonBar())
      }
    }
    return buttonBars[position]
  }

  private fun addKeyButton(buttons: MutableList<IExtraButton>?, button: IExtraButton) {
    if (buttons != null && !buttons.contains(button)) {
      buttons.add(button)
    }
  }

  private fun addKeyButton(contentView: LinearLayout, extraButton: IExtraButton) {
    val outerButton = extraButton.makeButton(context, null, android.R.attr.buttonBarButtonStyle)

    val param = LinearLayout.LayoutParams(
      calculateButtonWidth(),
      context.resources.getDimensionPixelSize(R.dimen.eks_height)
    )
    param.setMargins(0, 0, 0, 0)

    outerButton.layoutParams = param
    outerButton.maxLines = 1
    outerButton.typeface = typeface
    outerButton.text = extraButton.displayText
    outerButton.setPadding(0, 0, 0, 0)
    outerButton.setTextColor(IExtraButton.NORMAL_TEXT_COLOR)
    outerButton.isAllCaps = false

    outerButton.setOnClickListener {
      val root = rootView
      extraButton.onClick(root)
    }
    contentView.addView(outerButton)
  }

  private fun initBuiltinKeys() {

    // 3rd Row
    addBuiltinKey(TAB)
    addBuiltinKey(CTRL)
    addBuiltinKey(PAGE_DOWN)
    addBuiltinKey(ARROW_LEFT)
    addBuiltinKey(ARROW_DOWN)
    addBuiltinKey(ARROW_RIGHT)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      addBuiltinKey(EXPAND_BUTTONS)
    } else {
      addBuiltinKey(SLASH)
    }

    // Second Row
    addBuiltinKey(ESC)
    addBuiltinKey(ALT)
    addBuiltinKey(PAGE_UP)
    addBuiltinKey(HOME)
    addBuiltinKey(ARROW_UP)
    addBuiltinKey(END)
    addBuiltinKey(ControlButton(IExtraButton.KEY_DEL))

    // First Row
    addBuiltinKey(TextButton("~", false))
    addBuiltinKey(TextButton("-", false))
    addBuiltinKey(TextButton("`", false))
    addBuiltinKey(TextButton("$", false))
    addBuiltinKey(TextButton("\\", false))
    addBuiltinKey(TextButton("/", false))
    addBuiltinKey(TextButton("|", false))
    
    addBuiltinKey(ControlButton(IExtraButton.KEY_F8))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F9))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F10))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F11))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F12))
    addBuiltinKey(TextButton("< >", false))
    addBuiltinKey(TextButton("[ ]", false))

    addBuiltinKey(ControlButton(IExtraButton.KEY_F1))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F2))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F3))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F4))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F5))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F6))
    addBuiltinKey(ControlButton(IExtraButton.KEY_F7))

  }

  private fun calculateButtonWidth(): Int {
    return context.resources.displayMetrics.widthPixels / ExtraKeysView.MAX_BUTTONS_PER_LINE
  }
}
