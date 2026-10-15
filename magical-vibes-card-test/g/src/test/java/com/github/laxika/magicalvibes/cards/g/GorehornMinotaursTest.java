package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GorehornMinotaurs.class, Shock.class})
class GorehornMinotaursTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 2: enters with two +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castMinotaurs();

        assertThat(findPermanent(player1, "Gorehorn Minotaurs")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodthirst 2: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castMinotaurs();

        assertThat(findPermanent(player1, "Gorehorn Minotaurs")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 2 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castMinotaurs();

        assertThat(findPermanent(player1, "Gorehorn Minotaurs")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst checks damage dealt in response to the creature spell")
    void damageInResponseEnablesBloodthirst() {
        harness.castFromHand(player1, new GorehornMinotaurs(), "{2}{R}{R}");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gorehorn Minotaurs")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life loss without damage does not enable bloodthirst")
    void lifeLossDoesNotEnableBloodthirst() {
        harness.setLife(player2, 17);
        castMinotaurs();

        assertThat(findPermanent(player1, "Gorehorn Minotaurs")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst applies immediately even when the creature was not cast")
    void bloodthirstAppliesToNoncastEntryWithFixedCounterCount() {
        gd.recordDamageToPlayer(player2.getId(), 7);

        var permanent = harness.enterBattlefieldAndReturn(player1, new GorehornMinotaurs());

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
    private void castMinotaurs() {
        harness.castFromHand(player1, new GorehornMinotaurs(), "{2}{R}{R}");
        resolveAllTriggers();
    }
}
