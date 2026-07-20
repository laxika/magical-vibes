package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThresherLizardTest extends BaseCardTest {

    // ===== No boost: two or more cards in hand =====

    @Test
    @DisplayName("Base 3/2 with two cards in hand")
    void noBoostWithTwoCards() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addToBattlefield(player1, new ThresherLizard());

        Permanent lizard = findLizard();
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
    }

    // ===== Boost: one or fewer cards in hand =====

    @Test
    @DisplayName("Gets +1/+2 with exactly one card in hand")
    void boostWithOneCard() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new ThresherLizard());

        Permanent lizard = findLizard();
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+2 with an empty hand")
    void boostWithEmptyHand() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ThresherLizard());

        Permanent lizard = findLizard();
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(4);
    }

    // ===== Boost tracks hand size changes =====

    @Test
    @DisplayName("Loses boost when a second card enters hand")
    void losesBoostWhenHandGrows() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new ThresherLizard());

        Permanent lizard = findLizard();
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(4);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
    }

    // ===== Opponent's hand size is irrelevant =====

    @Test
    @DisplayName("Opponent's hand size does not affect the boost")
    void opponentHandDoesNotCount() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new ThresherLizard());

        Permanent lizard = findLizard();
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent findLizard() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Thresher Lizard"))
                .findFirst().orElseThrow();
    }
}
