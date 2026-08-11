package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GutsplitterGangTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the first main phase trigger blights a creature you control")
    void acceptingTriggerBlightsCreature() {
        var gang = harness.addToBattlefieldAndReturn(player1, new GutsplitterGang());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gang.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Declining the first main phase trigger causes you to lose 3 life")
    void decliningTriggerLosesLife() {
        harness.addToBattlefield(player1, new GutsplitterGang());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("The trigger does not happen during an opponent's first main phase")
    void doesNotTriggerOnOpponentsMainPhase() {
        harness.addToBattlefield(player1, new GutsplitterGang());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
