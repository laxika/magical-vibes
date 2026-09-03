package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SoldeviSimulacrum.class)
class SoldeviSimulacrumTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Soldevi Simulacrum")
    void paysCumulativeUpkeep() {
        Permanent simulacrum = harness.addToBattlefieldAndReturn(player1, new SoldeviSimulacrum());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(simulacrum.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(simulacrum);
    }

    @Test
    @DisplayName("Cumulative upkeep costs two mana on the second upkeep")
    void secondUpkeepCostsTwoMana() {
        Permanent simulacrum = harness.addToBattlefieldAndReturn(player1, new SoldeviSimulacrum());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(simulacrum.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(simulacrum);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during its controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent simulacrum = harness.addToBattlefieldAndReturn(player1, new SoldeviSimulacrum());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(simulacrum.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(simulacrum);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Soldevi Simulacrum")
    void declineSacrifices() {
        Permanent simulacrum = harness.addToBattlefieldAndReturn(player1, new SoldeviSimulacrum());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(simulacrum);
        harness.assertInGraveyard(player1, "Soldevi Simulacrum");
    }

    @Test
    @DisplayName("Activating the ability gives +1/+0 until end of turn")
    void abilityBoostsSelf() {
        Permanent simulacrum = addCreatureReady(player1, new SoldeviSimulacrum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(simulacrum.getPowerModifier()).isEqualTo(1);
        assertThat(simulacrum.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent simulacrum = addCreatureReady(player1, new SoldeviSimulacrum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(simulacrum.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(simulacrum.getPowerModifier()).isEqualTo(0);
    }

}
