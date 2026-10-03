package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BloodscaleProwler.class)
class BloodscaleProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1: enters with a +1/+1 counter when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castProwler();

        assertThat(findPermanent(player1, "Bloodscale Prowler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1 always adds only one counter, even after more damage")
    void bloodthirstAddsOnlyOneCounter() {
        gd.recordDamageToPlayer(player2.getId(), 5);
        castProwler();

        assertThat(findPermanent(player1, "Bloodscale Prowler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1: enters without a counter when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castProwler();

        assertThat(findPermanent(player1, "Bloodscale Prowler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);
        castProwler();

        assertThat(findPermanent(player1, "Bloodscale Prowler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst checks damage when the creature enters, after it was cast")
    void bloodthirstChecksDamageAtResolution() {
        harness.castFromHand(player1, new BloodscaleProwler(), "{2}{R}");
        gd.recordDamageToPlayer(player2.getId(), 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bloodscale Prowler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodthirst applies immediately when entering without being cast")
    void bloodthirstAppliesWithoutCasting() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        var prowler = harness.enterBattlefieldAndReturn(player1, new BloodscaleProwler());

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodthirst does not add a counter for damage dealt after entry")
    void bloodthirstDoesNotApplyRetroactively() {
        castProwler();
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Bloodscale Prowler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst ignores life loss without damage")
    void bloodthirstIgnoresLifeLoss() {
        harness.setLife(player2, 15);
        castProwler();

        assertThat(findPermanent(player1, "Bloodscale Prowler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castProwler() {
        harness.castFromHand(player1, new BloodscaleProwler(), "{2}{R}");
        resolveAllTriggers();
    }
}
