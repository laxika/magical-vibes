package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SafeholdDuo;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LockjawSnapper.class, GrizzlyBears.class, SafeholdDuo.class})
class LockjawSnapperTest extends BaseCardTest {

    /**
     * Sets up combat where Lockjaw Snapper (player1) attacks and is blocked by a 3/3 creature (player2),
     * so the Snapper dies from combat damage.
     */
    private void setupCombatWhereSnapperDies() {
        Permanent snapperPerm = findPermanent(player1, "Lockjaw Snapper");
        snapperPerm.setSummoningSick(false);
        snapperPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Death trigger adds a -1/-1 counter to each creature that already has one; others unaffected")
    void deathTriggerAddsCounterToWoundedCreatures() {
        harness.addToBattlefield(player1, new LockjawSnapper());

        GrizzlyBears woundedBear = new GrizzlyBears();
        woundedBear.setPower(4);
        woundedBear.setToughness(4);
        Permanent wounded = harness.addToBattlefieldAndReturn(player2, woundedBear);
        wounded.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        GrizzlyBears healthyBear = new GrizzlyBears();
        Permanent healthy = harness.addToBattlefieldAndReturn(player2, healthyBear);

        setupCombatWhereSnapperDies();

        harness.passBothPriorities(); // Combat damage — Snapper dies, trigger goes on stack
        harness.passBothPriorities(); // Resolve the death trigger

        harness.assertInGraveyard(player1, "Lockjaw Snapper");
        assertThat(wounded.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        assertThat(healthy.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Death trigger reduces a wounded 1/1 to 0/0 and it dies via SBA")
    void deathTriggerKillsWoundedCreature() {
        harness.addToBattlefield(player1, new LockjawSnapper());

        GrizzlyBears woundedBear = new GrizzlyBears(); // 2/2
        Permanent wounded = harness.addToBattlefieldAndReturn(player2, woundedBear);
        UUID woundedId = wounded.getId();
        wounded.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1); // now a 1/1

        setupCombatWhereSnapperDies();

        harness.passBothPriorities(); // Combat damage — Snapper dies, trigger on stack
        harness.passBothPriorities(); // Resolve the death trigger — wounded bear -> 0/0

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(woundedId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Wither leaves counters instead of marked damage on a surviving blocker")
    void witherDamageAndDeathTriggerAffectBlocker() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new LockjawSnapper());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SafeholdDuo());
        snapper.setSummoningSick(false);
        snapper.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lockjaw Snapper");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Safehold Duo");
    }

    @Test
    @DisplayName("Death trigger checks counters at resolution and affects both controllers")
    void deathTriggerChecksCurrentCountersOnBothSides() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new LockjawSnapper());
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new SafeholdDuo());
        Permanent newlyWounded = harness.addToBattlefieldAndReturn(player2, new SafeholdDuo());
        Permanent healed = harness.addToBattlefieldAndReturn(player2, new SafeholdDuo());
        Permanent otherCounter = harness.addToBattlefieldAndReturn(player2, new SafeholdDuo());
        friendly.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        healed.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        otherCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        snapper.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Lockjaw Snapper");
        newlyWounded.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        healed.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(friendly.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(newlyWounded.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(healed.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(otherCounter.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(otherCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
