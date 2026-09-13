package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WhirlpoolRider.class)
class WhirlpoolRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability wheels only its controller's hand")
    void entersWheelsOnlyItsControllersHand() {
        harness.setHand(player1, List.of(new WhirlpoolRider(), new WhirlpoolRider(), new WhirlpoolRider()));
        harness.setHand(player2, List.of(new WhirlpoolRider()));
        harness.setLibrary(player1, List.of(new WhirlpoolRider(), new WhirlpoolRider(), new WhirlpoolRider()));
        harness.setLibrary(player2, List.of(new WhirlpoolRider(), new WhirlpoolRider(), new WhirlpoolRider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gameLogContains(player1.getUsername() + " shuffles 2 cards from hand into their library.")).isTrue();
        assertThat(gameLogContains(player1.getUsername() + " draws 2 cards.")).isTrue();
        assertThat(gameLogContains(player2.getUsername() + " shuffles")).isFalse();
    }

    @Test
    @DisplayName("Its enters-the-battlefield ability draws nothing when its controller has no other cards in hand")
    void entersWithNoOtherCardsInHand() {
        harness.setHand(player1, List.of(new WhirlpoolRider()));
        harness.setHand(player2, List.of(new WhirlpoolRider()));
        harness.setLibrary(player1, List.of(new WhirlpoolRider(), new WhirlpoolRider()));
        harness.setLibrary(player2, List.of(new WhirlpoolRider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gameLogContains(player1.getUsername() + " has no cards in hand to shuffle.")).isTrue();
    }
}
