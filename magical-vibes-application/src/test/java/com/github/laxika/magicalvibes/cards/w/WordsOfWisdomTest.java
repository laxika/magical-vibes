package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WordsOfWisdom.class)
class WordsOfWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("Controller draws two cards and each other player draws one")
    void controllerDrawsTwoAndOtherPlayersDrawOne() {
        harness.setLibrary(player1, List.of(new WordsOfWisdom(), new WordsOfWisdom()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new WordsOfWisdom()));

        harness.castFromHand(player1, new WordsOfWisdom(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Words of Wisdom");
    }

    @Test
    @DisplayName("The nonactive caster draws two cards and the active player draws one")
    void nonactiveCasterDrawsTwo() {
        WordsOfWisdom firstDraw = new WordsOfWisdom();
        WordsOfWisdom secondDraw = new WordsOfWisdom();
        WordsOfWisdom otherPlayerDraw = new WordsOfWisdom();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(otherPlayerDraw));
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));

        harness.castFromHand(player2, new WordsOfWisdom(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherPlayerDraw);
        harness.assertInGraveyard(player2, "Words of Wisdom");
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The other player still draws before the caster loses for an insufficient library")
    void otherPlayerDrawsBeforeCasterLoses() {
        WordsOfWisdom casterDraw = new WordsOfWisdom();
        WordsOfWisdom otherPlayerDraw = new WordsOfWisdom();
        harness.setLibrary(player1, List.of(casterDraw));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(otherPlayerDraw));

        harness.castFromHand(player1, new WordsOfWisdom(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casterDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(otherPlayerDraw);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Both players lose together when both libraries are empty")
    void emptyLibrariesCauseDrawnGame() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new WordsOfWisdom(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
