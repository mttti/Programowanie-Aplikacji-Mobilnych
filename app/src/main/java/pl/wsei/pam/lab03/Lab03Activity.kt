package pl.wsei.pam.lab03

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import pl.wsei.pam.lab01.R
import java.util.Stack
import java.util.Timer
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        val size = intent.getIntArrayExtra("size") ?: intArrayOf(3, 3)

        val mBoard: GridLayout = findViewById(R.id.memory)
        mBoard.columnCount = size[0]
        mBoard.rowCount = size[1]

        if (savedInstanceState != null) {
            //kod odczytu stanu z savedInstanceState
            //utworznie modelu planszy i przywrócenia stanu zapisanego przed oborotem
        } else {
            //kod pierwszego uruchomienia aktywności, utworzenie modelu planszy

        val mBoardModel = MemoryBoardView(gridLayout = mBoard, cols = size[0], rows = size[1])

        mBoardModel.setOnGameChangeListener { e ->
            run {
                when (e.state) {
                    GameStates.Matching -> {
                        for (tile in e.tiles) {
                            tile.revealed = true
                        }
                    }

                    GameStates.Match -> {
                        for (tile in e.tiles) {
                            tile.revealed = true
                        }
                    }

                    GameStates.NoMatch -> {

                        for (tile in e.tiles) {
                            tile.revealed = true
                            Timer().schedule(2000) {
                                runOnUiThread() {
                                    tile.revealed = false
                                }
                            }

                        }
                    }

                    GameStates.Finished -> {

                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("state", "game state")
    }

    data class Tile(val button: ImageButton, val tileResource: Int, val deckResource: Int) {
        init {
            button.setImageResource(deckResource)
        }

        private var _revealed: Boolean = false
        var revealed: Boolean
            get() {
                return _revealed
            }
            set(value) {
                _revealed = value
                if (_revealed) {
                    button.setImageResource(tileResource)
                } else {
                    button.setImageResource(deckResource)
                }
            }

        fun removeOnClickListener() {
            button.setOnClickListener(null)
        }
    }

    enum class GameStates {
        Matching, Match, NoMatch, Finished
    }

    class MemoryGameLogic(private val maxMatches: Int) {

        private var valueFunctions: MutableList<() -> Int> = mutableListOf()

        private var _matches: Int = 0

        fun process(value: () -> Int): GameStates {
            if (valueFunctions.size < 1) {
                valueFunctions.add(value)
                return GameStates.Matching
            }
            valueFunctions.add(value)
            val result = valueFunctions[0]() == valueFunctions[1]()
            _matches += if (result) 1 else 0
            valueFunctions.clear()
            return when (result) {
                true -> if (_matches == maxMatches) GameStates.Finished else GameStates.Match
                false -> GameStates.NoMatch
            }
        }
    }

    data class MemoryGameEvent(
        val tiles: List<Tile>,
        val state: GameStates
    ) {
    }

    class MemoryBoardView(
        private val gridLayout: GridLayout,
        private val cols: Int,
        private val rows: Int
    ) {
        val tiles: MutableMap<String, Tile> = mutableMapOf()
        private val icons: List<Int> = listOf(
            R.drawable.baseline_rocket_launch_24,
            R.drawable.baseline_audiotrack_24,
            R.drawable.baseline_dark_mode_24,
            R.drawable.baseline_cookie_24,
            R.drawable.baseline_coffee_24,
            R.drawable.baseline_emoji_emotions_24,
            R.drawable.baseline_electric_car_24,
            R.drawable.baseline_headset_24,
            R.drawable.baseline_hail_24,
            R.drawable.baseline_girl_24,
            R.drawable.baseline_hotel_24,
            R.drawable.baseline_home_24,
            R.drawable.baseline_heart_broken_24,
            R.drawable.baseline_recommend_24,
            R.drawable.baseline_propane_24,
            R.drawable.baseline_place_24,
            R.drawable.baseline_snowshoeing_24,
            R.drawable.baseline_sign_language_24,
            R.drawable.baseline_sick_24
            // dodaj kolejne identyfikatory utworzonych ikon
        )

        init {
            val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
                it.addAll(icons.subList(0, cols * rows / 2))
                it.addAll(icons.subList(0, cols * rows / 2))
                it.shuffle()
            }

            for (row in 0..rows - 1) {
                for (col in 0..cols - 1) {
                    var btn = ImageButton(gridLayout.context).also {
                        it.tag = "${row}x${col}"
                        val layoutParams = GridLayout.LayoutParams()
                        it.setImageResource(R.drawable.baseline_rocket_launch_24)
                        layoutParams.width = 0
                        layoutParams.height = 0
                        layoutParams.setGravity(Gravity.CENTER)
                        layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                        layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                        it.layoutParams = layoutParams
                        gridLayout.addView(it)
                    }


                    val icon = shuffledIcons.last()
                    addTile(btn, icon)
                    if (shuffledIcons.size > 1) {
                        shuffledIcons.removeLast()
                    }
                }
            }
        }
//
//        fun setState(state) {}


        private val deckResource: Int = R.drawable.baseline_deck_24
        private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { (e) -> }
        private val matchedPair: Stack<Tile> = Stack()
        private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

        private fun onClickTile(v: View) {
            val tile = tiles[v.tag]
            matchedPair.push(tile)
            val matchResult = logic.process {
                tile?.tileResource ?: -1
            }
            onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
            if (matchResult != GameStates.Matching) {
                matchedPair.clear()
            }
        }

        fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
            onGameChangeStateListener = listener
        }

        private fun addTile(button: ImageButton, resourceImage: Int) {
            button.setOnClickListener(::onClickTile)
            val tile = Tile(button, resourceImage, deckResource)
            tiles[button.tag.toString()] = tile
        }
    }
}