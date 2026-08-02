package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AureliaTheWarleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps every creature you control and grants an additional combat phase")
    void attackUntapsAllCreaturesAndGrantsExtraCombat() {
        Permanent aurelia = addCreatureReady(player1, new AureliaTheWarleader());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedBear = addCreatureReady(player1, new GrizzlyBears());
        tappedBear.tap(); // a creature that stayed home, not an attacker

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(bear.isTapped()).isTrue();

        harness.passBothPriorities(); // resolve the trigger; play runs on into the granted phase

        // Untap is "all creatures you control", so the non-attacker untaps too. Vigilance keeps
        // Aurelia untapped throughout.
        assertThat(bear.isTapped()).isFalse();
        assertThat(tappedBear.isTapped()).isFalse();
        assertThat(aurelia.isTapped()).isFalse();

        // The additional combat phase followed directly, with no postcombat main phase between.
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking a second time in the same turn does not trigger again")
    void secondAttackSameTurnDoesNotTrigger() {
        addCreatureReady(player1, new AureliaTheWarleader());

        declareAttackers(player1, List.of(0), 1);
        harness.passBothPriorities();

        // Aurelia is now attacking again in the extra combat phase she created. "For the first time
        // each turn" gates the ability, so nothing new goes on the stack and no third combat phase
        // is queued.
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Aurelia, the Warleader"));
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking for the first time in a later combat phase still triggers")
    void firstAttackInLaterCombatPhaseStillTriggers() {
        Permanent aurelia = addCreatureReady(player1, new AureliaTheWarleader());

        // Aurelia sat out the turn's first combat and attacks for the first time in the second one:
        // the gate is per-creature "first time each turn", not "first combat phase".
        declareAttackers(player1, List.of(0), 2);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Aurelia, the Warleader"));
        assertThat(aurelia.isTapped()).isFalse(); // vigilance
    }

    @Test
    @DisplayName("The once-each-turn gate resets, so Aurelia triggers again on a later turn")
    void triggersAgainOnALaterTurn() {
        addCreatureReady(player1, new AureliaTheWarleader());

        declareAttackers(player1, List.of(0), 1);
        harness.passBothPriorities();

        gd.onceEachTurnAttackTriggersFiredThisTurn.clear(); // as the turn-start cleanup does

        declareAttackers(player1, List.of(0), 1);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Aurelia, the Warleader"));
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
