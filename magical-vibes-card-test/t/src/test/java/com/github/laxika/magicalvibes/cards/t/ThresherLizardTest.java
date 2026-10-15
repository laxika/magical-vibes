package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThresherLizard.class})
class ThresherLizardTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/2 with two cards in hand")
    void noBoostWithTwoCards() {
        harness.setHand(player1, List.of(new ThresherLizard(), new ThresherLizard()));
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new ThresherLizard());
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+2 with exactly one card in hand")
    void boostWithOneCard() {
        harness.setHand(player1, List.of(new ThresherLizard()));
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new ThresherLizard());
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+2 with an empty hand")
    void boostWithEmptyHand() {
        harness.setHand(player1, List.of());
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new ThresherLizard());
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses boost when a second card enters hand")
    void losesBoostWhenHandGrows() {
        harness.setHand(player1, List.of(new ThresherLizard()));
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new ThresherLizard());
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(4);

        harness.setHand(player1, List.of(new ThresherLizard(), new ThresherLizard()));
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's hand size does not affect the boost")
    void opponentHandDoesNotCount() {
        harness.setHand(player1, List.of(new ThresherLizard(), new ThresherLizard()));
        harness.setHand(player2, List.of());
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new ThresherLizard());
        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains the boost immediately when hand size falls to one")
    void gainsBoostWhenHandShrinks() {
        harness.setHand(player1, List.of(new ThresherLizard(), new ThresherLizard()));
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new ThresherLizard());

        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(2);

        harness.setHand(player1, List.of(new ThresherLizard()));

        assertThat(gqs.getEffectivePower(gd, lizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lizard)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Lizard checks its own controller and receives only its own bonus")
    void bonusesAreIndependentForEachController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new ThresherLizard(), new ThresherLizard()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThresherLizard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ThresherLizard());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ThresherLizard());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(2);

        harness.setHand(player1, List.of(new ThresherLizard(), new ThresherLizard()));
        harness.setHand(player2, List.of(new ThresherLizard()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(4);
    }
}
