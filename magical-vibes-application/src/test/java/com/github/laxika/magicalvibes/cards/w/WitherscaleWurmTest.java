package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.PowerOfFire;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitherscaleWurm.class, SafeholdSentry.class, PowerOfFire.class})
class WitherscaleWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When the Wurm blocks a creature, that attacker gains wither")
    void blocksCreatureGrantsWither() {
        Permanent attacker = addCreatureReady(player1, new SafeholdSentry());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WitherscaleWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(attacker.getGrantedKeywords()).contains(Keyword.WITHER);
    }

    @Test
    @DisplayName("When the Wurm becomes blocked by a creature, that blocker gains wither")
    void becomesBlockedGrantsWither() {
        Permanent wurm = addCreatureReady(player1, new WitherscaleWurm());
        wurm.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SafeholdSentry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getGrantedKeywords()).contains(Keyword.WITHER);
    }

    @Test
    @DisplayName("When the Wurm deals combat damage to an opponent, all its -1/-1 counters are removed")
    void dealingDamageRemovesMinusCounters() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new WitherscaleWurm());
        wurm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        wurm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of()); // no blockers — 6/6 hits the player
        harness.passBothPriorities(); // advance to combat damage; damage trigger goes on the stack
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each creature blocking the Wurm gains wither")
    void multipleBlockersEachGainWither() {
        Permanent wurm = addCreatureReady(player1, new WitherscaleWurm());
        wurm.setAttacking(true);
        Permanent first = addCreatureReady(player2, new SafeholdSentry());
        Permanent second = addCreatureReady(player2, new SafeholdSentry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, first, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("Granted wither makes combat damage place counters on the Wurm")
    void blockingCreatureDealsWitherDamage() {
        Permanent attacker = addCreatureReady(player1, new SafeholdSentry());
        attacker.setAttacking(true);
        Permanent wurm = addCreatureReady(player2, new WitherscaleWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);
    }

    @Test
    @DisplayName("The granted wither expires at end of turn")
    void grantedWitherExpires() {
        Permanent attacker = addCreatureReady(player1, new SafeholdSentry());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WitherscaleWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.WITHER)).isTrue();

        attacker.setDamagePreventionShield(9);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent removes only minus counters")
    void noncombatDamageToOpponentRemovesMinusCounters() {
        Permanent wurm = addCreatureReady(player1, new WitherscaleWurm());
        wurm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        wurm.setCounterCount(CounterType.CHARGE, 2);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(wurm.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(wurm.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to the Wurm's controller does not remove its minus counters")
    void damageToControllerDoesNotRemoveCounters() {
        Permanent wurm = addCreatureReady(player1, new WitherscaleWurm());
        wurm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(wurm.getId());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage to a creature does not remove the Wurm's minus counters")
    void damageToCreatureDoesNotRemoveCounters() {
        Permanent wurm = addCreatureReady(player1, new WitherscaleWurm());
        wurm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        Permanent creature = addCreatureReady(player2, new SafeholdSentry());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(wurm.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }
}
