package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LightningReaverTest extends BaseCardTest {

    // ===== Combat damage: put a charge counter on it =====

    @Test
    @DisplayName("Gets a charge counter when it deals combat damage to a player")
    void getsChargeCounterOnCombatDamage() {
        Permanent reaver = addReadyReaver();
        reaver.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage

        assertThat(gd.getLife(player2.getId())).isEqualTo(17); // 3/3 unblocked

        harness.passBothPriorities(); // resolve the combat damage trigger

        assertThat(reaver.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    // ===== End step: deals damage equal to charge counters to each opponent =====

    @Test
    @DisplayName("At end step, deals damage equal to its charge counters to each opponent")
    void endStepDealsDamageEqualToChargeCounters() {
        Permanent reaver = addReadyReaver();
        reaver.setCounterCount(CounterType.CHARGE, 2);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve the end step trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("With no charge counters, the end step trigger deals no damage")
    void endStepWithNoCountersDealsNoDamage() {
        addReadyReaver();
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    // ===== Full loop: combat damage accrues a counter, then the end step burns for it =====

    @Test
    @DisplayName("Combat damage adds a counter, then the end step deals that much to the opponent")
    void combatCounterThenEndStepBurn() {
        Permanent reaver = addReadyReaver();
        reaver.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage: 20 -> 17
        harness.passBothPriorities(); // resolve counter trigger

        assertThat(reaver.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN -> END_STEP, trigger fires
        harness.passBothPriorities(); // resolve end step trigger: 17 -> 16

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    // ===== Helpers =====

    private Permanent addReadyReaver() {
        Permanent perm = new Permanent(new LightningReaver());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN -> END_STEP, triggers fire
    }
}
