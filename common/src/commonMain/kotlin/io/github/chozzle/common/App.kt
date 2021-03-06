package io.github.chozzle.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.ui.Modifier
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun App() {
    val gridSize = GridSize(width = 20, height = 20)
    val squareStates = remember { mutableMapOf<Location, SquareState>() }

    for (column in 0..gridSize.width) {
        for (row in 0..gridSize.height) {
            val isCurrentSquareBomb = Random.nextInt(from = 0, until = 100) < PERCENT_BOMB_CHANCE
            if (isCurrentSquareBomb) {
                squareStates[Location(column, row)] = SquareState(isClicked = false, isBomb = isCurrentSquareBomb)
            }
        }
    }
    val gameState = remember { mutableStateOf<GameState>(GameState.NotStarted) }

    MaterialTheme {
        Column {
            Button(onClick = { gameState.value = GameState.NotStarted }) {
                Text("Restart")
            }
            Table(
                gridSize = gridSize,
                squareStates = squareStates,
                gameState = gameState
            )
        }
    }
}

private const val PERCENT_BOMB_CHANCE = 20

data class GridSize(val width: Int, val height: Int)
data class Location(val column: Int, val row: Int)
sealed class GameState {
    object NotStarted : GameState()
    data class Started(val squareStates: Map<Location, SquareState>) : GameState()
    object Dead : GameState()
}
data class SquareState(val isClicked: Boolean, val isBomb: Boolean)

@Composable
fun Table(
    gridSize: GridSize,
    squareStates: MutableMap<Location, SquareState>,
    gameState: MutableState<GameState>
) {
    Row {
        val columns = 0..gridSize.width
        val rows = 0..gridSize.height
        for (column in columns) {
            Column {
                for (row in rows) {
                    val currentLocation = Location(column, row)
                    var isClicked by remember { mutableStateOf(false) }
                    when (gameState.value) {
                        GameState.NotStarted -> isClicked = false
                        GameState.Dead -> isClicked = true
                    }

                    Square(
                        isBomb = squareStates[currentLocation]?.isBomb ?: false,
                        countBombsSurrounding = squareStates.countBombsSurrounding(currentLocation),
                        bombClicked = {
                            print("BOOM")
                            gameState.value = GameState.Dead
                        },
                        isClicked = isClicked,
                        squareClicked = {
                            if (gameState.value == GameState.NotStarted) gameState.value = GameState.Started()
                            if (currentLocation )
                            isClicked = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Square(
    isBomb: Boolean,
    countBombsSurrounding: Int,
    bombClicked: () -> Unit,
    isClicked: Boolean,
    squareClicked: () -> Unit
) {
    Surface(
        Modifier.size(24.dp)
            .clickable {
                if (isBomb) bombClicked()
                squareClicked()
            },
        color = if (isClicked) Color.LightGray else Color.White,
        elevation = 2.dp,
    ) {
        if (!isClicked) return@Surface
        Box(contentAlignment = Alignment.Center) {
            Text(
                when {
                    isBomb -> "\uD83D\uDCA3"
                    countBombsSurrounding == 0 -> ""
                    else -> countBombsSurrounding.toString()
                },
            )
        }
    }
}

private fun Map<Location, SquareState>.countBombsSurrounding(location: Location): Int {
    val surroundingLocations = listOf(
        location.copy(column = location.column - 1, row = location.row - 1),
        location.copy(row = location.row - 1),
        location.copy(column = location.column + 1, row = location.row - 1),
        location.copy(column = location.column + 1),
        location.copy(column = location.column + 1, row = location.row + 1),
        location.copy(row = location.row + 1),
        location.copy(column = location.column - 1, row = location.row + 1),
        location.copy(column = location.column - 1)
    )
    return surroundingLocations.count { get(it)?.isBomb ?: false  }
}