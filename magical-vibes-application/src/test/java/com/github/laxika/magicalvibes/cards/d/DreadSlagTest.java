package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadSlag.class, GrizzlyBears.class})
class DreadSlagTest extends BaseCardTest {

    @Test
    @DisplayName("Has its printed power and toughness with an empty controller hand")
    void fullSizeWithEmptyControllerHand() {
        harness.setHand(player1, List.of());
        Permanent dreadSlag = addDreadSlag(player1);

        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(9);
    }

    @Test
    @DisplayName("Gets -4/-4 for each card in its controller's hand")
    void shrinksWithControllerHandSize() {
        harness.setHand(player1, handOf(2));
        Permanent dreadSlag = addDreadSlag(player1);

        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only its controller's hand")
    void ignoresOpponentsHand() {
        harness.setHand(player1, handOf(1));
        harness.setHand(player2, handOf(4));
        Permanent dreadSlag = addDreadSlag(player1);

        assertThat(gqs.getEffectivePower(gd, dreadSlag)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(5);
    }

    @Test
    @DisplayName("Updates dynamically as its controller's hand changes")
    void updatesDynamically() {
        harness.setHand(player1, handOf(1));
        Permanent dreadSlag = addDreadSlag(player1);
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(5);

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectiveToughness(gd, dreadSlag)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dies when three cards reduce its toughness to zero")
    void diesWhenReducedToZeroToughness() {
        harness.setHand(player1, handOf(3));
        addDreadSlag(player1);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Dread Slag");
        harness.assertInGraveyard(player1, "Dread Slag");
    }

    private Permanent addDreadSlag(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DreadSlag());
    }

    private List<Card> handOf(int count) {
        return new ArrayList<>(IntStream.range(0, count)
                .mapToObj(i -> (Card) new GrizzlyBears())
                .toList());
    }
}
