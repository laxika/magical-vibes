package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlumberingWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two -1/-1 counters")
    void entersWithCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SlumberingWalker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Slumbering Walker")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a counter creates a reflexive trigger that returns a power-two creature")
    void removesCounterAndReturnsMatchingCreature() {
        addWalkerWithCounters();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Slumbering Walker")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the counter removal does nothing")
    void decliningRemovalDoesNothing() {
        addWalkerWithCounters();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Slumbering Walker")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards with power two or less can be returned")
    void onlyMatchingPowerCanBeReturned() {
        addWalkerWithCounters();
        harness.setGraveyard(player1, List.of(new HillGiant()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Slumbering Walker")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Chooses among multiple legal graveyard targets")
    void choosesAmongLegalTargets() {
        addWalkerWithCounters();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new ChildOfNight()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Child of Night");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addWalkerWithCounters() {
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new SlumberingWalker());
        walker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        return walker;
    }
}
