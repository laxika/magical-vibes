package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurybornHellkite.class})
class FurybornHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 6: enters with six +1/+1 counters when an opponent was dealt any damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castHellkite();

        assertThat(findPermanent(player1, "Furyborn Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bloodthirst 6: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castHellkite();

        assertThat(findPermanent(player1, "Furyborn Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 6 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 5);
        castHellkite();

        assertThat(findPermanent(player1, "Furyborn Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst gives exactly six counters even after more than six damage")
    void largerDamageStillGivesSixCounters() {
        gd.recordDamageToPlayer(player2.getId(), 10);
        castHellkite();

        assertThat(findPermanent(player1, "Furyborn Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bloodthirst checks damage when the creature enters, rather than when it is cast")
    void damageAfterCastingEnablesBloodthirst() {
        harness.castFromHand(player1, new FurybornHellkite(), "{4}{R}{R}{R}");
        assertThat(gd.stack).hasSize(1);
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Furyborn Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bloodthirst applies immediately when Hellkite enters without being cast")
    void bloodthirstAppliesWithoutCasting() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        var hellkite = harness.enterBattlefieldAndReturn(player1, new FurybornHellkite());

        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    private void castHellkite() {
        harness.castFromHand(player1, new FurybornHellkite(), "{4}{R}{R}{R}");
        resolveAllTriggers();
    }
}
