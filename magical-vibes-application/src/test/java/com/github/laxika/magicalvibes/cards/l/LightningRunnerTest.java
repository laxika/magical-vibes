package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightningRunnerTest extends BaseCardTest {

    @Test
    void gainsEnergyAndCanPayToUntapControlledCreaturesAndGetAnExtraCombat() {
        Permanent runner = addCreatureReady(player1, new LightningRunner());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        nonAttacker.tap();
        opponentCreature.tap();
        gd.playerEnergyCounters.put(player1.getId(), 6);

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(runner.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(runner.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(nonAttacker.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    void decliningPaymentKeepsTheEnergyAndLeavesCreaturesTapped() {
        Permanent runner = addCreatureReady(player1, new LightningRunner());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 6);

        declareAttackers(player1, List.of(0, 1), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);
        assertThat(runner.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    void cannotPayEightEnergyAfterGainingOnlyTwo() {
        Permanent runner = addCreatureReady(player1, new LightningRunner());
        gd.playerEnergyCounters.put(player1.getId(), 5);

        declareAttackers(player1, List.of(0), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(7);
        assertThat(runner.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
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
