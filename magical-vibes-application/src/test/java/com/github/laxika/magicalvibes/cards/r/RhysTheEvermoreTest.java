package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RhysTheEvermore.class, GrizzlyBears.class, Shock.class})
class RhysTheEvermoreTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants persist to another creature you control")
    void etbGrantsPersist() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castRhys(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Granted persist returns the creature with a -1/-1 counter")
    void grantedPersistReturnsCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castRhys(bears.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist grant wears off at end of turn")
    void persistGrantWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castRhys(bears.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Activated ability removes the chosen number of counters across counter types")
    void removesChosenNumberOfCounters() {
        addCreatureReady(player1, new RhysTheEvermore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setCounterCount(CounterType.CHARGE, 1);
        prepareAbility();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice = (PendingInteraction.XValueChoice)
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
                + bears.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability does not prompt when the target has no counters")
    void noPromptWithoutCounters() {
        addCreatureReady(player1, new RhysTheEvermore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        prepareAbility();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Activated ability only targets creatures you control")
    void rejectsOpponentCreature() {
        addCreatureReady(player1, new RhysTheEvermore());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing a total must still allow choosing which kinds of counters to remove")
    void choosingTotalDoesNotAutomaticallyChooseCounterKinds() {
        addCreatureReady(player1, new RhysTheEvermore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        prepareAbility();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.interaction.isAwaitingInput())
                .as("The controller must be allowed to choose the -1/-1 counter instead of the charge counter")
                .isTrue();
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rhys can target himself and choose to remove zero counters")
    void canTargetSelfAndRemoveZero() {
        Permanent rhys = addCreatureReady(player1, new RhysTheEvermore());
        rhys.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        prepareAbility();

        harness.activateAbility(player1, 0, 0, null, rhys.getId());
        assertThat(rhys.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(rhys.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Activated ability can remove all -1/-1 counters")
    void removesAllMinusOneCounters() {
        addCreatureReady(player1, new RhysTheEvermore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        prepareAbility();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Activated ability cannot be used outside a main phase")
    void rejectsActivationDuringCombat() {
        Permanent rhys = addCreatureReady(player1, new RhysTheEvermore());
        prepareAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rhys.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rhys.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability cannot be used on an opponent's turn")
    void rejectsActivationOnOpponentsTurn() {
        Permanent rhys = addCreatureReady(player1, new RhysTheEvermore());
        prepareAbility();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rhys.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rhys.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability cannot be used while a spell is on the stack")
    void rejectsActivationWithNonemptyStack() {
        Permanent rhys = addCreatureReady(player1, new RhysTheEvermore());
        prepareAbility();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rhys.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rhys.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granted persist does not trigger if the creature already has a -1/-1 counter")
    void persistDoesNotReturnCreatureWithMinusOneCounter() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        castRhys(bears.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature returned by the granted persist does not retain that grant")
    void returnedCreatureDoesNotRetainPersist() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castRhys(bears.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Rhys can be cast on the opponent's turn and grant persist")
    void flashAllowsCastingOnOpponentsTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castRhys(bears.getId());

        harness.assertOnBattlefield(player1, "Rhys, the Evermore");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("The persist grant survives Rhys leaving the battlefield")
    void persistGrantSurvivesSourceLeaving() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castRhys(bears.getId());
        Permanent rhys = findPermanent(player1, "Rhys, the Evermore");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, rhys.getId());

        harness.assertNotOnBattlefield(player1, "Rhys, the Evermore");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Rhys enters without granting persist to himself when no other creature is present")
    void entersWithoutAnotherCreature() {
        harness.setHand(player1, List.of(new RhysTheEvermore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent rhys = findPermanent(player1, "Rhys, the Evermore");
        assertThat(gqs.hasKeyword(gd, rhys, Keyword.PERSIST)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ETB ability still grants persist if Rhys dies before it resolves")
    void etbResolvesAfterSourceDies() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RhysTheEvermore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent rhys = findPermanent(player1, "Rhys, the Evermore");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, rhys.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rhys, the Evermore");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("The ETB grant cannot save its target if the target dies before resolution")
    void etbDoesNotSaveTargetBeforeResolution() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RhysTheEvermore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rhys cannot pay the tap cost while tapped")
    void rejectsActivationWhileTapped() {
        Permanent rhys = addCreatureReady(player1, new RhysTheEvermore());
        rhys.setTapped(true);
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rhys.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rhys cannot pay the tap cost with summoning sickness")
    void rejectsActivationWithSummoningSickness() {
        Permanent rhys = addCreatureReady(player1, new RhysTheEvermore());
        rhys.setSummoningSick(true);
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, rhys.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rhys.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castRhys(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RhysTheEvermore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
