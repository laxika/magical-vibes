package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritedCompanion.class, Forest.class})
class SpiritedCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("When Spirited Companion enters, its controller draws a card")
    void etbDrawsOneCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new SpiritedCompanion(), "{1}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The enter trigger draws only when it resolves")
    void drawWaitsForTriggerResolution() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.castFromHand(player1, new SpiritedCompanion(), "{1}{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spirited Companion");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A Companion controlled by the second player draws for that player only")
    void secondPlayerDrawsForTheirOwnCompanion() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player2, new SpiritedCompanion(), "{1}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
