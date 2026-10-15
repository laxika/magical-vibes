package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinestHour.class, GrizzlyBears.class})
class FinestHourTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone in the first combat phase untaps that creature and grants a second combat phase")
    void attacksAloneFirstCombatUntapsAndGrantsExtraCombat() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FinestHour());

        declareAttackers(player1, List.of(0), 1);
        assertThat(bear.isTapped()).isTrue(); // attacking taps it

        harness.passBothPriorities(); // resolve the trigger; play runs on into the granted phase

        // The extra combat phase materialised: play advanced straight into the turn's SECOND combat
        // phase (declare-attackers) on the same player's turn, never stopping at a postcombat main
        // phase in between. "That creature" was untapped, so it is ready to attack again.
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A queued combat-only phase loops End of Combat straight back into combat, skipping the postcombat main phase")
    void additionalCombatPhaseSkipsPostcombatMain() {
        gd.additionalCombatPhasesOnly = 1; // as Finest Hour's trigger would leave it
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        // Unlike Relentless Assault's combat+main pair (consumed at the postcombat main), this loops
        // back to another combat phase directly from End of Combat — no main phase in between.
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(0);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1); // entering combat bumped the counter
    }

    @Test
    @DisplayName("Attacking alone in a later combat phase does not untap or grant another combat phase")
    void attacksAloneSecondCombatDoesNothing() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FinestHour());

        declareAttackers(player1, List.of(0), 2); // already the turn's second combat phase
        harness.passBothPriorities(); // resolve exalted; the extra-combat ability must not trigger

        assertThat(bear.isTapped()).isTrue();               // not untapped
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);   // no third combat phase was created
    }

    @Test
    @DisplayName("Attacking with another creature (not alone) does not trigger Finest Hour")
    void attacksWithAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FinestHour());

        declareAttackers(player1, List.of(0, 1), 1);

        // The attacks-alone condition fails, so no Finest Hour ability goes on the stack at all.
        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Finest Hour"));
    }

    @Test
    void laterCombatTriggersOnlyExalted() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FinestHour());

        declareAttackers(player1, List.of(0), 2);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void exaltedBonusesAccumulateAcrossBothCombats() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FinestHour());

        declareAttackers(player1, List.of(0), 1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);

        declareAttackers(player1, List.of(0), 2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    void multipleCopiesEachGrantACombatButDoNotUntapAtItsBeginning() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FinestHour());
        harness.addToBattlefield(player1, new FinestHour());

        declareAttackers(player1, List.of(0), 1);
        harness.passBothPriorities();

        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
        assertThat(bear.isTapped()).isFalse();

        declareAttackers(player1, List.of(0), 2);
        harness.passBothPriorities();

        assertThat(gd.combatPhasesThisTurn).isEqualTo(3);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(bear.isTapped()).isTrue();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        gd.combatPhasesThisTurn = combatPhaseNumber;
        declareAttackers(player, attackerIndices);
    }
}
