package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RetaliatorGriffin.class, LightningBolt.class, GrizzlyBears.class})
class RetaliatorGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell damage lets you add that many +1/+1 counters when accepted")
    void opponentSpellDamageAddsCountersWhenAccepted() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId()); // Lightning Bolt resolves — 3 damage to player1
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

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage from your own source does not trigger the ability")
    void ownSourceDoesNotTrigger() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Player1 damages themselves with their own Lightning Bolt — "a source an opponent
        // controls" is not satisfied, so nothing triggers.
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

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

    @Test
    @DisplayName("Simultaneous combat damage from two sources gives separate optional counter additions")
    void simultaneousSourcesTriggerSeparately() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        for (int i = 0; i < 2; i++) {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RetaliatorGriffin());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
        }
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Damage to the opponent does not trigger your Griffin")
    void damageToOtherPlayerDoesNotTrigger() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A Griffin killed in response to its trigger receives no counters")
    void removedGriffinReceivesNoCounters() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RetaliatorGriffin());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, griffin.getId());
        harness.assertInGraveyard(player1, "Retaliator Griffin");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Retaliator Griffin");
        assertThat(griffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
