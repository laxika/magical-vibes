package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyssaOfTraken.class, MindStone.class, GrizzlyBears.class})
class NyssaOfTrakenTest extends BaseCardTest {

    @Test
    void sacrificesArtifactsTapsUpToThatManyCreaturesAndDrawsThatManyCards() {
        Permanent nyssa = addCreatureReady(player1, new NyssaOfTraken());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nyssa)));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNotNull();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(firstArtifact.getId(), secondArtifact.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, firstTarget.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstArtifact.getId())
                        || permanent.getId().equals(secondArtifact.getId()));
    }

    @Test
    void sacrificingNoArtifactsDoesNotCreateReflexiveAbility() {
        Permanent nyssa = addCreatureReady(player1, new NyssaOfTraken());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nyssa)));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canDeclineToSacrificeAnAvailableArtifact() {
        Permanent nyssa = addCreatureReady(player1, new NyssaOfTraken());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nyssa)));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canChooseNoTargetsAndStillDrawForEachSacrificedArtifact() {
        Permanent nyssa = addCreatureReady(player1, new NyssaOfTraken());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nyssa)));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void drawingUsesSacrificeCountRatherThanNumberOfChosenTargets() {
        Permanent nyssa = addCreatureReady(player1, new NyssaOfTraken());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nyssa)));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(firstArtifact.getId(), secondArtifact.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void canTargetMoreThanOneHundredCreaturesWhenEnoughArtifactsAreSacrificed() {
        Permanent nyssa = addCreatureReady(player1, new NyssaOfTraken());
        List<Permanent> artifacts = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new MindStone()))
                .toList();
        List<Permanent> targets = IntStream.range(0, 101)
                .mapToObj(i -> addCreatureReady(player2, new GrizzlyBears()))
                .toList();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, IntStream.range(0, 101)
                .mapToObj(i -> new GrizzlyBears()).toList());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nyssa)));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1,
                artifacts.stream().map(Permanent::getId).toList());
        for (Permanent target : targets) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        resolveAllTriggers();

        assertThat(targets).allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(101);
    }

    @Test
    void controllerKeepsMoreThanSevenCardsDuringCleanup() {
        harness.addToBattlefield(player1, new NyssaOfTraken());
        harness.setHand(player1, List.of(new MindStone(), new MindStone(), new MindStone(),
                new MindStone(), new MindStone(), new MindStone(), new MindStone(), new MindStone()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    void opponentStillDiscardsToSevenDuringCleanup() {
        harness.addToBattlefield(player1, new NyssaOfTraken());
        harness.setHand(player2, List.of(new MindStone(), new MindStone(), new MindStone(),
                new MindStone(), new MindStone(), new MindStone(), new MindStone(), new MindStone()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }
}
