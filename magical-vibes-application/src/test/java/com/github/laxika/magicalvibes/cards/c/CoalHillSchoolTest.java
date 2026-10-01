package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalHillSchool.class, GrizzlyBears.class, Spellbook.class})
class CoalHillSchoolTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new CoalHillSchool(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void eachPlayerDrawsWhenTheyCastAHistoricSpell() {
        Card player1Draw = new GrizzlyBears();
        Card player2Draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void nonHistoricSpellDoesNotTriggerDraw() {
        Card player1Draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(player1Draw);
    }

    @Test
    void chaosReturnsATargetHistoricCardFromYourGraveyard() {
        Card nonHistoric = new GrizzlyBears();
        Card historic = new Spellbook();
        Card opponentHistoric = new Spellbook();
        harness.setGraveyard(player1, List.of(nonHistoric, historic));
        harness.setGraveyard(player2, List.of(opponentHistoric));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellGraveyardTargetTrigger(gd));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(historic.getId());

        harness.handleMultipleCardsChosen(player1, List.of(historic.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonHistoric);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentHistoric);
    }
}
