package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMaelstrom.class, Forest.class, GrizzlyBears.class, Shock.class})
class TheMaelstromTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheMaelstrom(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void upkeepMayRevealAndPutPermanentOntoBattlefield() {
        Card permanent = new Forest();
        harness.setLibrary(player1, List.of(permanent));

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(candidate -> candidate.getCard().getId().equals(permanent.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningToRevealLeavesTheLibraryUnchanged() {
        Card permanent = new Forest();
        harness.setLibrary(player1, List.of(permanent));

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(permanent);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(candidate -> candidate.getCard().getId().equals(permanent.getId()));
    }

    @Test
    void revealingNonPermanentPutsItOnTheBottom() {
        Card nonPermanent = new Shock();
        Card next = new Forest();
        harness.setLibrary(player1, List.of(nonPermanent, next));

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, nonPermanent);
    }

    @Test
    void planeswalkingToThePlaneOffersTheReveal() {
        Card permanent = new Forest();
        harness.setLibrary(player1, List.of(permanent));
        gd.planechase.deck.addFirst(new TheMaelstrom());

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(candidate -> candidate.getCard().getId().equals(permanent.getId()));
    }

    @Test
    void chaosReturnsTargetPermanentFromItsControllersGraveyard() {
        Card nonPermanent = new Shock();
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonPermanent, permanent));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellGraveyardTargetTrigger(gd));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(permanent.getId());

        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(candidate -> candidate.getCard().getId().equals(permanent.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(nonPermanent);
    }

    private void triggerUpkeep() {
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
