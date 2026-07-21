package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RetaliatorGriffinTest extends BaseCardTest {

    // ===== Damage from an opponent's source adds that many +1/+1 counters =====

    @Test
    @DisplayName("Opponent's spell damage lets you add that many +1/+1 counters when accepted")
    void opponentSpellDamageAddsCountersWhenAccepted() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities(); // Lightning Bolt resolves — 3 damage to player1
        harness.passBothPriorities(); // trigger resolves → "you may" prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the trigger adds no counters")
    void decliningAddsNoCounters() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // ===== Only opponent-controlled sources trigger it =====

    @Test
    @DisplayName("Damage from your own source does not trigger the ability")
    void ownSourceDoesNotTrigger() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Player1 damages themselves with their own Lightning Bolt — "a source an opponent
        // controls" is not satisfied, so nothing triggers.
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // ===== Combat damage from an opponent's attacker triggers it =====

    @Test
    @DisplayName("Combat damage from an opponent's attacker adds that many +1/+1 counters")
    void opponentCombatDamageAddsCounters() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        harness.setLife(player1, 20);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage (2 to player1) and advance the trigger to its "you may" prompt.
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
