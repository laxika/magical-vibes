package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GutsplitterGang.class})
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

    @Test
    void cannotAvoidLifeLossWhenNoCreatureRemains() {
        var gang = harness.addToBattlefieldAndReturn(player1, new GutsplitterGang());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPrecombatMain(player1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gang);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player1, lifeBefore - 3);
    }

    @Test
    void canBlightAnotherControlledCreatureButNotOpponentsCreature() {
        var gang = harness.addToBattlefieldAndReturn(player1, new GutsplitterGang());
        var opponentGang = harness.addToBattlefieldAndReturn(player2, new GutsplitterGang());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPrecombatMain(player1);
        var otherGang = harness.addToBattlefieldAndReturn(player1, new GutsplitterGang());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherGang.getId());

        assertThat(otherGang.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gang.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opponentGang.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void doesNotTriggerDuringPostcombatMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.addToBattlefield(player1, new GutsplitterGang());

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
