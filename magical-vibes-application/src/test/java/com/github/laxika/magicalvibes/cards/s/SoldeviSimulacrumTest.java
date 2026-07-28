package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent simulacrum = addReadySimulacrum();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(simulacrum.getPowerModifier()).isEqualTo(1);
        assertThat(simulacrum.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent simulacrum = addReadySimulacrum();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(simulacrum.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(simulacrum.getPowerModifier()).isEqualTo(0);
    }

    private Permanent addReadySimulacrum() {
        Permanent perm = new Permanent(new SoldeviSimulacrum());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
