package io.github.chozzle.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.ui.Modifier
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random


private const val PERCENT_BOMB_CHANCE = 20

data class GridSize(val width: Int, val height: Int)
data class Location(val column: Int, val row: Int)
sealed class GameState {
    object Started : GameState()
    object GameOver : GameState()

    companion object {
        fun newGame(gridSize: GridSize): SnapshotStateMap<Location, SquareState> {
            val squareStates = mutableStateMapOf<Location, SquareState>()

            for (column in 0..gridSize.width) {
                for (row in 0..gridSize.height) {
                    val isCurrentSquareBomb = Random.nextInt(from = 0, until = 100) < PERCENT_BOMB_CHANCE
                    if (isCurrentSquareBomb) {
                        squareStates[Location(column, row)] =
                            SquareState(isClicked = false, isBomb = isCurrentSquareBomb)
                    }
                }
            }
            return squareStates
        }
    }
}

data class SquareState(val isClicked: Boolean, val isBomb: Boolean) {
    companion object
}

@Composable
fun App() {
    val gridSize = GridSize(width = 20, height = 20)

    var gameState by remember {
        mutableStateOf<GameState>(GameState.Started)
    }
    var squareState by remember {
        mutableStateOf(GameState.newGame(gridSize))
    }

    MaterialTheme {
        Column {
            // Restart button
            Button(onClick = {
                gameState = GameState.Started
                squareState = GameState.newGame(gridSize)
            }) {
                Text(modifier = Modifier.padding(24.dp), text = "Restart")
            }

            Table(
                gridSize = gridSize,
                gameState = gameState,
                bombClicked = {
                    gameState = GameState.GameOver
                },
                squareState = squareState
            )
        }
    }
}

@Composable
fun Table(
    gridSize: GridSize,
    gameState: GameState,
    bombClicked: () -> Unit,
    squareState: SnapshotStateMap<Location, SquareState>
) {
    Row {
        val columns = 0..gridSize.width
        val rows = 0..gridSize.height
        for (column in columns) {
            Column {
                for (row in rows) {
                    val currentLocation = Location(column, row)

                    Square(
                        isBomb = squareState[currentLocation]?.isBomb ?: false,
                        countBombsSurrounding = squareState.countBombsSurrounding(currentLocation),
                        bombClicked = {
                            bombClicked()
                        },
                        isClicked = when (gameState) {
                            GameState.Started -> squareState[currentLocation]?.isClicked ?: false
                            GameState.GameOver -> true
                        },
                        squareClicked = {
                            squareState[currentLocation] =
                                squareState[currentLocation]?.copy(isClicked = true)
                                    ?: SquareState(
                                        isBomb = false,
                                        isClicked = true
                                    )
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
    return surroundingLocations.count { get(it)?.isBomb ?: false }
}