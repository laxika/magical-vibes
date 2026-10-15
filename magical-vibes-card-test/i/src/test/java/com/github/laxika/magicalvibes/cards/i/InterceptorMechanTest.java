package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AuxiliaryBoosters;
import com.github.laxika.magicalvibes.cards.b.BygoneColossus;
import com.github.laxika.magicalvibes.cards.c.ConsultTheStarCharts;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StationMonitor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InterceptorMechan.class, AuxiliaryBoosters.class, StationMonitor.class,
        ConsultTheStarCharts.class, Forest.class, BygoneColossus.class})
class InterceptorMechanTest extends BaseCardTest {

    @Test
    void etbReturnsArtifactOrCreatureCardToHand() {
        AuxiliaryBoosters artifact = new AuxiliaryBoosters();
        StationMonitor creature = new StationMonitor();
        harness.setGraveyard(player1, List.of(artifact, creature));

        castMechan();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Auxiliary Boosters");
        harness.assertInGraveyard(player1, "Station Monitor");
    }

    @Test
    void etbDoesNotTargetNonArtifactNonCreatureCard() {
        ConsultTheStarCharts nonPermanent = new ConsultTheStarCharts();
        harness.setGraveyard(player1, List.of(nonPermanent));

        castMechan();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Consult the Star Charts");
    }

    @Test
    void putsCounterAtEndStepAfterNonlandPermanentLeaves() {
        Permanent mechan = addReadyMechan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StationMonitor());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCounterAfterOnlyLandLeaves() {
        Permanent mechan = addReadyMechan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        advanceToEndStep();

        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void etbReturnsCreatureButExcludesOpponentsGraveyardAndLands() {
        StationMonitor creature = new StationMonitor();
        AuxiliaryBoosters opposingArtifact = new AuxiliaryBoosters();
        harness.setGraveyard(player1, List.of(creature, new Forest()));
        harness.setGraveyard(player2, List.of(opposingArtifact));

        castMechan();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Station Monitor");
        harness.assertNotInGraveyard(player1, "Station Monitor");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Auxiliary Boosters");
    }

    @Test
    void etbDoesNotReturnTargetThatLeftGraveyardBeforeResolution() {
        StationMonitor creature = new StationMonitor();
        harness.setGraveyard(player1, List.of(creature));
        castMechan();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Station Monitor");
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void doesNotTriggerWhenNeitherVoidConditionOccurred() {
        Permanent mechan = addReadyMechan();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        Permanent mechan = addReadyMechan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StationMonitor());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonlandLeavingAfterEndStepBeginsDoesNotCreateVoidTrigger() {
        Permanent mechan = addReadyMechan();
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StationMonitor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        assertThat(gd.stack).isEmpty();
        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsOnlyOneCounterWhenMultipleNonlandsLeave() {
        Permanent mechan = addReadyMechan();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StationMonitor());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AuxiliaryBoosters());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });

        advanceToEndStep();

        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void warpEnablesVoidBeforeWarpedPermanentLeavesBattlefield() {
        Permanent mechan = addReadyMechan();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BygoneColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bygone Colossus");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(mechan.getCard().getId()));
        for (int remaining = 3; remaining > 0 && !gd.stack.isEmpty(); remaining--) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();
        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void departureBeforeMechanEntersStillEnablesVoid() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new StationMonitor());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        Permanent mechan = addReadyMechan();

        advanceToEndStep();

        assertThat(mechan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castMechan() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new InterceptorMechan(), "{2}{B}{R}");
        harness.passBothPriorities();
    }

    private Permanent addReadyMechan() {
        return harness.addToBattlefieldAndReturn(player1, new InterceptorMechan());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
