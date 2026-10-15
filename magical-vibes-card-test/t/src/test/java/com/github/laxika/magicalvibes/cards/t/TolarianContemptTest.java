package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.cards.g.GrafRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MidnightScavengers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolarianContempt.class, GrizzlyBears.class, Island.class,
        GrafRats.class, MidnightScavengers.class, ChitteringHost.class})
class TolarianContemptTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts a rejection counter on each opponent creature")
    void enteringPutsRejectionCounterOnEachOpponentCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTolarianContempt();

        assertThat(ownCreature.getCounterCount(CounterType.REJECTION)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.REJECTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("At the controller's end step it targets one marked creature per opponent")
    void endStepTargetsOneMarkedCreaturePerOpponent() {
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        castTolarianContempt();
        Permanent unmarkedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(markedCreature.getId())
                .doesNotContain(unmarkedCreature.getId());

        harness.handlePermanentChosen(player1, markedCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unmarkedCreature)
                .doesNotContain(markedCreature);
        assertThat(gd.playerHands.get(player2.getId())).contains(existingTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(markedCreature.getCard());
    }

    @Test
    @DisplayName("The controller may choose no creature even when marked creatures exist")
    void canDeclineTargeting() {
        Permanent marked = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTolarianContempt();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handlePermanentChosen(player1, player1.getId());
            harness.passBothPriorities();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(marked);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only one of two marked creatures controlled by the same opponent is rejected")
    void rejectsOnlyOneCreaturePerOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTolarianContempt();

        assertThat(first.getCounterCount(CounterType.REJECTION)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.REJECTION)).isEqualTo(1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handlePermanentChosen(player1, first.getId());
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
            harness.handleListChoice(player2, "Top");
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(first.getCard());
    }

    @Test
    @DisplayName("The creature's owner chooses the destination even when an opponent controls it")
    void ownerChoosesForCreatureControlledByOpponent() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player1.getId());
        Permanent marked = harness.addToBattlefieldAndReturn(player2, creature);
        Card previousTop = new Island();
        harness.setLibrary(player1, List.of(previousTop));
        castTolarianContempt();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handlePermanentChosen(player1, marked.getId());
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                    .playerId()).isEqualTo(player1.getId());
            harness.handleListChoice(player1, "Top");
        });

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, previousTop);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(marked);
    }

    @Test
    @DisplayName("Removing the rejection counter before resolution makes the target illegal")
    void rechecksRejectionCounterOnResolution() {
        Permanent marked = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTolarianContempt();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handlePermanentChosen(player1, marked.getId());
            marked.setCounterCount(CounterType.REJECTION, 0);
            harness.passBothPriorities();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(marked);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not trigger at an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent marked = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTolarianContempt();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(marked);
    }

    @Test
    @CardUsed({GrafRats.class, MidnightScavengers.class, ChitteringHost.class})
    @DisplayName("The owner of a melded creature can choose to put both components on the bottom")
    void meldedCreatureOwnerCanChooseBottom() {
        Card rats = new GrafRats();
        Card scavengers = new MidnightScavengers();
        rats.setOwnerId(player2.getId());
        scavengers.setOwnerId(player2.getId());
        Permanent host = harness.addToBattlefieldAndReturn(player2, new ChitteringHost());
        host.getMeldComponentCards().addAll(List.of(rats, scavengers));
        Card previousTop = new GrafRats();
        harness.setLibrary(player2, List.of(previousTop));
        castTolarianContempt();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handlePermanentChosen(player1, host.getId());
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
            harness.handleListChoice(player2, "Bottom");
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(host);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(previousTop);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(rats, scavengers);
    }

    private void castTolarianContempt() {
        harness.castFromHand(player1, new TolarianContempt(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
