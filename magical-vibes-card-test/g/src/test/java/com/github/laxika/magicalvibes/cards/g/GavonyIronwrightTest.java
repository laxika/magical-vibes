package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GavonyIronwright.class, GrizzlyBears.class})
class GavonyIronwrightTest extends BaseCardTest {

    @Test
    @DisplayName("No boost to other creatures at default 20 life")
    void noBoostAtDefaultLife() {
        harness.addToBattlefield(player1, new GavonyIronwright());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost at 6 life (just above threshold)")
    void noBoostAt6Life() {
        gd.playerLifeTotals.put(player1.getId(), 6);
        harness.addToBattlefield(player1, new GavonyIronwright());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts other creatures +1/+4 at exactly 5 life")
    void boostAtExactly5Life() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        harness.addToBattlefield(player1, new GavonyIronwright());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boosts other creatures below 5 life")
    void boostBelow5Life() {
        gd.playerLifeTotals.put(player1.getId(), 1);
        harness.addToBattlefield(player1, new GavonyIronwright());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not boost itself at 5 life")
    void doesNotBoostItself() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        Permanent ironwright = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());

        assertThat(gqs.getEffectivePower(gd, ironwright)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ironwright)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        harness.addToBattlefield(player1, new GavonyIronwright());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the controller's life total matters, not the opponent's")
    void opponentLifeDoesNotCount() {
        gd.playerLifeTotals.put(player2.getId(), 1);
        harness.addToBattlefield(player1, new GavonyIronwright());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains and loses the boost as life crosses the threshold")
    void boostIsDynamic() {
        harness.addToBattlefield(player1, new GavonyIronwright());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // 20 life — no boost
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        // Drop to 5 — boost applies
        gd.playerLifeTotals.put(player1.getId(), 5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);

        // Back above threshold — boost gone
        gd.playerLifeTotals.put(player1.getId(), 10);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Ironwrights boost each other and their bonuses add together")
    void multipleIronwrightsBoostEachOther() {
        harness.setLife(player1, 5);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(8);

        Permanent third = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());

        for (Permanent ironwright : java.util.List.of(first, second, third)) {
            assertThat(gqs.getEffectivePower(gd, ironwright)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, ironwright)).isEqualTo(12);
        }
    }

    @Test
    @DisplayName("Each controller's Ironwright uses that controller's life total")
    void eachControllerUsesTheirOwnLifeTotal() {
        harness.setLife(player1, 6);
        harness.setLife(player2, 5);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());
        harness.addToBattlefield(player1, new GavonyIronwright());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GavonyIronwright());
        harness.addToBattlefield(player2, new GavonyIronwright());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(8);

        harness.setLife(player1, 5);
        harness.setLife(player2, 6);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }
}
