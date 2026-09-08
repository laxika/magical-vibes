package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.t.Terror;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirlingDervish.class, FireWhip.class, GrizzlyBears.class, ScatheZombies.class, Terror.class})
class WhirlingDervishTest extends BaseCardTest {

    @Test
    void damageToAnOpponentsCreatureDoesNotQualify() {
        Permanent dervish = addDervish(player1);
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        Permanent fireWhip = new Permanent(new FireWhip());
        fireWhip.setAttachedTo(dervish.getId());
        gd.playerBattlefields.get(player1.getId()).add(fireWhip);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();
        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    /** Simulates the Dervish having dealt combat damage to a player this turn. */
    private void recordCombatDamageToPlayer(Permanent creature, UUID damagedPlayerId) {
        gd.recordDamageRecipientBySource(creature.getId(), damagedPlayerId);
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(damagedPlayerId);
    }

    private void advanceToEndStepAndResolve(UUID activePlayerId) {
        harness.forceActivePlayer(activePlayerId.equals(player1.getId()) ? player1 : player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step (queues any trigger), then let it resolve.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter at end step after dealing damage to an opponent")
    void getsCounterAfterDealingDamage() {
        Permanent dervish = addDervish(player1);
        recordCombatDamageToPlayer(dervish, player2.getId());

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets no counter when it dealt no damage this turn")
    void noCounterWithoutDamage() {
        Permanent dervish = addDervish(player1);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage dealt only to its own controller does not qualify")
    void noCounterWhenDamageNotToOpponent() {
        Permanent dervish = addDervish(player1);

        // Damage recorded against its own controller (not an opponent) — must not trigger.
        recordCombatDamageToPlayer(dervish, player1.getId());

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers on each end step, including the opponent's, when it dealt damage to an opponent")
    void triggersOnEachEndStep() {
        Permanent dervish = addDervish(player1);
        recordCombatDamageToPlayer(dervish, player2.getId());

        // It is player2's (the opponent's) turn.
        advanceToEndStepAndResolve(player2.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter after dealing noncombat damage to an opponent")
    void getsCounterAfterDealingNoncombatDamage() {
        Permanent dervish = addDervish(player1);
        Permanent fireWhip = new Permanent(new FireWhip());
        fireWhip.setAttachedTo(dervish.getId());
        gd.playerBattlefields.get(player1.getId()).add(fireWhip);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking")
    void protectionFromBlackPreventsBlocking() {
        Permanent dervish = addDervish(player1);
        dervish.setAttacking(true);
        addCreatureReady(player2, new ScatheZombies());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents a black spell from targeting it")
    void protectionFromBlackPreventsTargeting() {
        Permanent dervish = addDervish(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, dervish.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from a black creature")
    void protectionFromBlackPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player2, new ScatheZombies());
        attacker.setAttacking(true);
        Permanent dervish = addDervish(player1);
        dervish.setBlocking(true);
        dervish.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dervish);
        assertThat(dervish.getMarkedDamage()).isZero();
    }

    private Permanent addDervish(Player player) {
        return addCreatureReady(player, new WhirlingDervish());
    }
}
