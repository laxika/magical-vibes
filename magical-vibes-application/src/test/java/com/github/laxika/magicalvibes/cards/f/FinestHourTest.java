package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.passBothPriorities(); // the trigger resolves, but its intervening-"if" now fails

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

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
