package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceOfPowerTest extends BaseCardTest {

    private void castBalanceOfPower() {
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws cards equal to the difference when the opponent has more in hand")
    void drawsTheDifference() {
        // Balance of Power is on the stack while resolving, so the caster's hand is empty.
        harness.setHand(player1, new ArrayList<>(List.of(new BalanceOfPower())));
        harness.setHand(player2, new ArrayList<>(List.of(
                new Forest(), new GoblinPiker(), new GrizzlyBears())));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalanceOfPower();

        // Opponent 3, controller 0 -> draw 3.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Only the difference is drawn when the caster also holds cards")
    void drawsOnlyTheDifference() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new BalanceOfPower(), new Forest(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest())));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalanceOfPower();

        // Opponent 5, controller 2 (the two Forests still in hand) -> draw 3.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Draws nothing when hand sizes are equal")
    void drawsNothingWhenEqual() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new BalanceOfPower(), new Forest(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalanceOfPower();

        // Opponent 2, controller 2 -> draw 0.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Draws nothing when the caster has more cards than the opponent")
    void drawsNothingWhenCasterHasMore() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new BalanceOfPower(), new Forest(), new Forest(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalanceOfPower();

        // Opponent 1, controller 3 -> draw 0.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new BalanceOfPower())));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
