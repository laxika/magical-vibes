package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WildGuess;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChangeOfFortune.class, Forest.class, WildGuess.class})
class ChangeOfFortuneTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the hand, then draws one card for each card discarded this turn")
    void discardsHandThenDrawsCardsDiscardedThisTurn() {
        harness.setHand(player1, List.of(new ChangeOfFortune(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Change of Fortune");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Includes cards discarded earlier in the turn")
    void includesEarlierDiscardedCards() {
        harness.setHand(player1, List.of(new WildGuess(), new ChangeOfFortune(), new Forest()));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorceryWithDiscard(player1, 0, 2);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }
}
