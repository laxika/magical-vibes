package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GroveOfTheDreampods.class, Forest.class, GrizzlyBears.class, GrafdiggersCage.class})
class GroveOfTheDreampodsTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new GroveOfTheDreampods(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void upkeepRevealsUntilCreaturePutsItOntoBattlefieldAndBottomsTheRest() {
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        Card secondForest = new Forest();
        harness.setLibrary(player1, List.of(forest, creature, secondForest));

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, secondForest);
    }

    @Test
    void planeswalkingToGroveOfTheDreampodsTriggersTheReveal() {
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, creature));
        gd.planechase.deck.addFirst(new GroveOfTheDreampods());

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void chaosReturnsTargetCreatureFromYourGraveyard() {
        Card noncreature = new Forest();
        Card creature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(noncreature, creature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellGraveyardTargetTrigger(gd));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(creature);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
    }

    @Test
    void revealLeavesUnrevealedCardsInOrderAboveTheBottomedCards() {
        Card firstRevealed = new Forest();
        Card secondRevealed = new Forest();
        Card creature = new GrizzlyBears();
        Card nextCard = new Forest();
        Card laterCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstRevealed, secondRevealed, creature, nextCard, laterCreature));

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(nextCard, laterCreature);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(firstRevealed, secondRevealed);
    }

    @Test
    void noCreatureRevealedReturnsEveryCardToTheLibrary() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryRevealsNothing() {
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void upkeepUsesTheNewActivePlayersLibraryAndBattlefield() {
        Card inactiveCreature = new GrizzlyBears();
        Card activeCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(inactiveCreature));
        harness.setLibrary(player2, List.of(activeCreature));
        harness.forceActivePlayer(player2);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(inactiveCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getCard())).containsExactly(activeCreature);
    }

    @Test
    void chaosDoesNotReturnAnotherCreatureWhenTheChosenTargetLeavesTheGraveyard() {
        Card target = new GrizzlyBears();
        Card otherCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, otherCreature));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellGraveyardTargetTrigger(gd));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
    }

    @Test
    void blockedCreatureRemainsOnTopWhileOtherRevealedCardsGoToTheBottom() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        Card revealedLand = new Forest();
        Card blockedCreature = new GrizzlyBears();
        Card unrevealedLand = new Forest();
        harness.setLibrary(player1, List.of(revealedLand, blockedCreature, unrevealedLand));

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(blockedCreature, unrevealedLand, revealedLand);
    }
}
