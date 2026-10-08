package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlumberingWalker.class, GrizzlyBears.class, HillGiant.class, ChildOfNight.class})
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

    @Test
    @DisplayName("No counter means no creature is returned")
    void cannotReturnCreatureWithoutRemovingCounter() {
        harness.addToBattlefield(player1, new SlumberingWalker());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A counter other than a -1/-1 counter can be removed")
    void removesStunCounterAndReturnsCreature() {
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new SlumberingWalker());
        walker.setCounterCount(CounterType.STUN, 1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(walker.getCounterCount(CounterType.STUN)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing a counter with an empty graveyard does not require a target")
    void removesCounterWithEmptyGraveyard() {
        Permanent walker = addWalkerWithCounters();
        harness.setGraveyard(player1, List.of());

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(walker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent's graveyard cannot supply the reflexive target")
    void cannotReturnOpponentsCreature() {
        Permanent walker = addWalkerWithCounters();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(walker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent walker = addWalkerWithCounters();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(walker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A source that leaves before the end-step ability resolves cannot remove a counter")
    void sourceLeavesBeforeCounterRemoval() {
        Permanent walker = addWalkerWithCounters();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        walker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 7);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Slumbering Walker");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The reflexive ability still returns its target after Slumbering Walker dies")
    void reflexiveAbilitySurvivesSourceLeaving() {
        Permanent walker = addWalkerWithCounters();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        walker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 7);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Slumbering Walker");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A target that leaves the graveyard cannot be returned, and the removed counter stays removed")
    void targetLeavesBeforeReflexiveResolution() {
        Permanent walker = addWalkerWithCounters();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private Permanent addWalkerWithCounters() {
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new SlumberingWalker());
        walker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        return walker;
    }
}
