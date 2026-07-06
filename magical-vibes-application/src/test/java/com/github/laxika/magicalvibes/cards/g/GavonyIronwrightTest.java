package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GavonyIronwrightTest extends BaseCardTest {

    // ===== Above threshold (default 20 life) =====

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

    // ===== At or below threshold =====

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

    // ===== Does not boost itself ("other" creatures) =====

    @Test
    @DisplayName("Does not boost itself at 5 life")
    void doesNotBoostItself() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        Permanent ironwright = harness.addToBattlefieldAndReturn(player1, new GavonyIronwright());

        assertThat(gqs.getEffectivePower(gd, ironwright)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ironwright)).isEqualTo(4);
    }

    // ===== Only affects your own creatures =====

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

    // ===== Boost is dynamic =====

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
}
