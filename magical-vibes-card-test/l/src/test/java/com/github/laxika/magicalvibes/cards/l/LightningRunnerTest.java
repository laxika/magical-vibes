package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EnragedGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningRunner.class, EnragedGiant.class})
class LightningRunnerTest extends BaseCardTest {

    @Test
    void gainsEnergyAndCanPayToUntapControlledCreaturesAndGetAnExtraCombat() {
        Permanent runner = addCreatureReady(player1, new LightningRunner());
        Permanent attacker = addCreatureReady(player1, new EnragedGiant());
        Permanent nonAttacker = addCreatureReady(player1, new EnragedGiant());
        Permanent opponentCreature = addCreatureReady(player2, new EnragedGiant());
        nonAttacker.tap();
        opponentCreature.tap();
        gd.playerEnergyCounters.put(player1.getId(), 6);

        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0, 1));
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
        Permanent attacker = addCreatureReady(player1, new EnragedGiant());
        gd.playerEnergyCounters.put(player1.getId(), 6);

        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0, 1));
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

        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(7);
        assertThat(runner.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    void paymentLeavesExcessEnergyAndFinishesCurrentCombatBeforeTheExtraCombat() {
        Permanent runner = addCreatureReady(player1, new LightningRunner());
        gd.playerEnergyCounters.put(player1.getId(), 14);
        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);
        assertThat(runner.isTapped()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(runner.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    void attackTriggerResolvesAfterRunnerLeavesTheBattlefield() {
        Permanent runner = addCreatureReady(player1, new LightningRunner());
        Permanent otherCreature = addCreatureReady(player1, new EnragedGiant());
        otherCreature.tap();
        gd.playerEnergyCounters.put(player1.getId(), 6);
        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(runner);
        gd.playerGraveyards.get(player1.getId()).add(runner.getCard());

        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    void energyAndUntappingBelongToTheAttackingController() {
        Permanent runner = addCreatureReady(player2, new LightningRunner());
        Permanent opponentCreature = addCreatureReady(player1, new EnragedGiant());
        opponentCreature.tap();
        gd.playerEnergyCounters.put(player2.getId(), 6);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.combatPhasesThisTurn = 1;
        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, true));

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isZero();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(runner.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }
}
