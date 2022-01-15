package indigo

import kotlin.system.exitProcess

class CardDeck {
    private val deck = mutableListOf<String>()
    private val ranks = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    private val suits = arrayOf("♠", "♥", "♦", "♣")

    init {
        reset()
    }

    fun reset() {
        deck.clear()
        for (suit in suits) {
            for (rank in ranks) {
                deck.add("$rank$suit")
            }
        }
    }

    fun size() = deck.size

    fun shuffle() {
        deck.shuffle()
    }

    fun getCards(numOfCards: Int): List<String> {
        val cards = mutableListOf<String>()
        if (numOfCards <= deck.size) {
            repeat(numOfCards) { cards.add(deck.removeAt(deck.lastIndex)) }
        }
        return cards
    }
}

class CardGame {
    private val deck = CardDeck()
    private val cardsInHandPlayer = mutableListOf<String>()
    private val cardsInHandComputer = mutableListOf<String>()
    private val cardsWonPlayer = mutableListOf<String>()
    private val cardsWonComputer = mutableListOf<String>()
    private val cardsOnTable = mutableListOf<String>()
    private var pointsPlayer = 0
    private var pointsComputer = 0
    private var turn = 0
    private var lastWin = 0

    fun reset(turn: Int): List<String> {
        this.turn = turn
        lastWin = this.turn
        pointsPlayer = 0
        pointsComputer = 0
        deck.reset()
        deck.shuffle()
        cardsInHandPlayer.clear()
        cardsInHandComputer.clear()
        cardsWonPlayer.clear()
        cardsWonComputer.clear()
        cardsOnTable.clear()
        cardsOnTable.addAll(deck.getCards(4))
        return cardsOnTable
    }

    fun deal(): List<String> {
        return if (deck.size() >= 12) {
            cardsInHandComputer.addAll(deck.getCards(6))
            cardsInHandPlayer.addAll(deck.getCards(6))
            cardsInHandPlayer
        } else
            emptyList()
    }

    fun playCardComputer(): String {
        for (card in cardsInHandComputer) print("$card ")
        println()

        val card = chooseCard()
        addCardOnTable(card)
        cardsInHandComputer.remove(card)

        println("Computer plays $card")
        if (cardsOnTable.size >= 2 && checkIfWin()) {
            lastWin = 1
            cardsWonComputer.addAll(cardsOnTable)
            pointsComputer += countPoints(cardsOnTable)
            cardsOnTable.clear()
            println("Computer wins cards")
            printScore()
        }
        return card
    }

    fun playCardPlayer(i: Int): String {
        val card = cardsInHandPlayer.removeAt(i - 1)
        addCardOnTable(card)
        if (cardsOnTable.size >= 2 && checkIfWin()) {
            lastWin = 0
            cardsWonPlayer.addAll(cardsOnTable)
            pointsPlayer += countPoints(cardsOnTable)
            cardsOnTable.clear()
            println("Player wins cards")
            printScore()
        }
        return card
    }

    private fun printScore() {
        println("Score: Player $pointsPlayer - Computer $pointsComputer")
        println("Cards: Player ${cardsWonPlayer.size} - Computer ${cardsWonComputer.size}")
    }

    fun getCardsOnTable(): List<String> {
        return cardsOnTable
    }

    fun getCardsInHandPlayer(): List<String> {
        return cardsInHandPlayer
    }

    private fun addCardOnTable(card: String) {
        cardsOnTable.add(card)
    }

    private fun checkIfWin(): Boolean {
        val a = getRankSuit(cardsOnTable.last())
        val b = getRankSuit(cardsOnTable[cardsOnTable.size - 2])
        return a.first == b.first || a.second == b.second
    }

    private fun getRankSuit(card:String): Pair<String, String> {
        return Pair(getRank(card), getSuit(card))
    }

    private fun countPoints(cards: List<String>): Int {
        val ranks = listOf("A", "10", "J", "Q", "K")
        var count = 0
        for (card in cards) {
            val rank = getRank(card)
            if (rank in ranks) count++
        }
        return count
    }

    fun finalScore() {
        if (lastWin == 0) {
            cardsWonPlayer.addAll(cardsOnTable)
            pointsPlayer += countPoints(cardsOnTable)
            cardsOnTable.clear()
        } else {
            cardsWonComputer.addAll(cardsOnTable)
            pointsComputer += countPoints(cardsOnTable)
            cardsOnTable.clear()
        }

        if (cardsWonComputer.size > cardsWonPlayer.size) pointsComputer += 3
        else if (cardsWonComputer.size < cardsWonPlayer.size) pointsPlayer += 3
        else if (cardsWonComputer.size == cardsWonPlayer.size) {
            if (lastWin == 0) pointsPlayer += 3
            else pointsComputer += 3
        }
        printScore()
    }

    private fun chooseCard(): String {
        val candidates = mutableListOf<String>()

        if (cardsInHandComputer.size == 1) return cardsInHandComputer.first()

        val topCard = cardsOnTable.lastOrNull() ?: return getSuitRankRandom(cardsInHandComputer)

        val topRank = getRank(topCard)
        val topSuit = getSuit(topCard)
        for (card in cardsInHandComputer) {
            if (getRank(card) == topRank || getSuit(card) == topSuit) candidates.add(card)
        }

        when (candidates.size) {
            0 -> {
                return getSuitRankRandom(cardsInHandComputer)
            }
            1 -> {
                return candidates.first()
            }
            else -> {
                var cardToPlay = getSameSuit(candidates)
                if (cardToPlay.isNotEmpty()) return cardToPlay.first()
                cardToPlay = getSameRank(candidates)
                return if (cardToPlay.isNotEmpty()) cardToPlay.first()
                else candidates.first()
            }
        }
    }

    private fun getSameSuit(cards: List<String>): List<String> {
        val similarCards = mutableListOf<String>()
        for (suit in listOf("♠", "♥", "♦", "♣")) {
            val c = cards.filter{ getSuit(it) == suit }
            if (c.size > 1) similarCards.addAll(c)
        }
        return similarCards
    }

    private fun getSameRank(cards: List<String>): List<String> {
        val similarCards = mutableListOf<String>()
        for (rank in listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")) {
            val r = cards.filter { getRank(it) == rank }
            if (r.size > 1) similarCards.addAll(r)
        }
        return similarCards
    }

    private fun getSuitRankRandom(cards: List<String>): String {
        for (i in 0 until cards.lastIndex) {
            for (j in i+1..cards.lastIndex) {
                if (getSuit(cards[i]) == getSuit(cards[j])) return cards[i]
            }
        }
        for (i in 0 until cards.lastIndex) {
            for (j in i+1..cards.lastIndex) {
                if (getRank(cards[i]) == getRank(cards[j])) return cards[i]
            }
        }
        return cards.first()
    }

    private fun getRank(card: String): String {
        return if (card.length == 2) card.substring(0, 1)
        else card.substring(0, 2)
    }

    private fun getSuit(card: String): String {
        return if (card.length == 2) card.substring(1, 2)
        else card.substring(2, 3)
    }
}

fun main() {
    var turn = 0
    val game = CardGame()

    println("Indigo Card Game")
    do {
        println("Play first?")
        val option = readLine()!!.lowercase().trim()
        if (option == "no") turn = 1
    } while (option != "yes" && option != "no")

    game.reset(turn)
    printInitialCardsOnTable(game.getCardsOnTable())

    repeat(4) {
        game.deal()
        repeat(6) {
            if (turn == 0) {
                playerPlayCard(game)
                computerPlayCard(game)
            } else {
                computerPlayCard(game)
                playerPlayCard(game)
            }
        }
    }
    printCardsOnTable(game.getCardsOnTable())
    game.finalScore()
    println("Game Over")
}

private fun playerPlayCard(game: CardGame) {
    printCardsOnTable(game.getCardsOnTable())
    printlnNumberedCards(game.getCardsInHandPlayer())
    var cardToPlay: Int?
    do {
        println("Choose a card to play (1-${game.getCardsInHandPlayer().size}):")
        val cardNum = readLine()!!
        if (cardNum.trim().lowercase() == "exit") {
            println("Game Over")
            exitProcess(0)
        }
        cardToPlay = cardNum.trim().toIntOrNull()
    } while (cardToPlay !in 1..game.getCardsInHandPlayer().size)
    game.playCardPlayer(cardToPlay!!)
    println()
}

private fun computerPlayCard(game: CardGame) {
    printCardsOnTable(game.getCardsOnTable())
    game.playCardComputer()
    println()
}

fun printCardsOnTable(cards: List<String>) {
    if (cards.isEmpty()) {
        println("No cards on the table")
    } else println("${cards.size} cards on the table, and the top card is ${cards.last()}")
}

fun printlnNumberedCards(cards: List<String>) {
    print("Cards in hand: ")
    for ((i, card) in cards.withIndex()) print("${i + 1})$card ")
    println()
}

fun printInitialCardsOnTable(cards: List<String>) {
    print("Initial cards on the table: ")
    for (card in cards) print("$card ")
    println()
    println()
}