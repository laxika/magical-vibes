package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class WhirlingDervishTest extends BaseCardTest {

    /** Simulates the Dervish having dealt combat damage to a player this turn. */
    private void recordCombatDamageToPlayer(Permanent creature, UUID damagedPlayerId) {
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
        Permanent dervish = new Permanent(new WhirlingDervish());
        gd.playerBattlefields.get(player1.getId()).add(dervish);

        recordCombatDamageToPlayer(dervish, player2.getId());

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets no counter when it dealt no damage this turn")
    void noCounterWithoutDamage() {
        Permanent dervish = new Permanent(new WhirlingDervish());
        gd.playerBattlefields.get(player1.getId()).add(dervish);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage dealt only to its own controller does not qualify")
    void noCounterWhenDamageNotToOpponent() {
        Permanent dervish = new Permanent(new WhirlingDervish());
        gd.playerBattlefields.get(player1.getId()).add(dervish);

        // Damage recorded against its own controller (not an opponent) — must not trigger.
        recordCombatDamageToPlayer(dervish, player1.getId());

        advanceToEndStepAndResolve(player1.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers on each end step, including the opponent's, when it dealt damage to an opponent")
    void triggersOnEachEndStep() {
        Permanent dervish = new Permanent(new WhirlingDervish());
        gd.playerBattlefields.get(player1.getId()).add(dervish);

        recordCombatDamageToPlayer(dervish, player2.getId());

        // It is player2's (the opponent's) turn.
        advanceToEndStepAndResolve(player2.getId());

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
