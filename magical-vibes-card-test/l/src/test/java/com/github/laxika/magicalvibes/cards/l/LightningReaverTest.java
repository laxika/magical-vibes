package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EthercasteKnight;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningReaver.class, Terminate.class, LeoninArmorguard.class, EthercasteKnight.class})
class LightningReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a charge counter when it deals combat damage to a player")
    void getsChargeCounterOnCombatDamage() {
        Permanent reaver = addReadyReaver();
        reaver.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17); // 3/3 unblocked

        harness.passBothPriorities(); // resolve the combat damage trigger

        assertThat(reaver.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }


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


    @Test
    @DisplayName("Combat damage adds a counter, then the end step deals that much to the opponent")
    void combatCounterThenEndStepBurn() {
        Permanent reaver = addReadyReaver();
        reaver.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities(); // resolve counter trigger

        assertThat(reaver.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve end step trigger: 17 -> 16

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void hasteAllowsAttackingOnTheTurnItEnters() {
        Permanent reaver = harness.addToBattlefieldAndReturn(player1, new LightningReaver());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(reaver.isAttacking()).isTrue();
        assertThat(reaver.isTapped()).isTrue();
    }
    @Test
    void fearRejectsNonblackNonartifactBlocker() {
        Permanent reaver = addReadyReaver();
        reaver.setAttacking(true);
        harness.addToBattlefield(player2, new LeoninArmorguard());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).isInstanceOf(IllegalStateException.class);
    }
    @Test
    void fearAllowsArtifactBlockerAndCreatureDamageDoesNotAddChargeCounter() {
        Permanent reaver = addReadyReaver();
        reaver.setAttacking(true);
        harness.addToBattlefield(player2, new EthercasteKnight());
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(reaver.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fearAllowsBlackBlocker() {
        Permanent reaver = addReadyReaver();
        reaver.setAttacking(true);
        harness.addToBattlefield(player2, new LightningReaver());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(reaver.isBlockedThisCombat()).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent reaver = addReadyReaver();
        reaver.setCounterCount(CounterType.CHARGE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void endStepUsesChargeCountersAtResolutionAndDoesNotAddAnotherCounter() {
        Permanent reaver = addReadyReaver();
        reaver.setCounterCount(CounterType.CHARGE, 1);
        reaver.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        reaver.setCounterCount(CounterType.CHARGE, 3);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(reaver.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void endStepUsesLastKnownCountersAfterSourceIsDestroyed() {
        Permanent reaver = addReadyReaver();
        reaver.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        reaver.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, reaver.getId());
        harness.assertNotOnBattlefield(player1, "Lightning Reaver");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void combatCounterTriggerCannotPutCountersOnDestroyedSource() {
        Permanent reaver = addReadyReaver();
        reaver.setAttacking(true);
        resolveCombat();
        assertThat(reaver.getCounterCount(CounterType.CHARGE)).isZero();

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, reaver.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lightning Reaver");
        assertThat(reaver.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyReaver() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new LightningReaver());
        perm.setSummoningSick(false);
        return perm;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN -> END_STEP, triggers fire
    }
}
