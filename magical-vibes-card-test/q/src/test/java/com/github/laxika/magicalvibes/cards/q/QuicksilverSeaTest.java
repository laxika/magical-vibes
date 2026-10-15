package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuicksilverSea.class, GrizzlyBears.class, LightningBolt.class, Forest.class, Fling.class})
class QuicksilverSeaTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new QuicksilverSea(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkingToQuicksilverSeaScriesFour() {
        List<Card> library = List.of(
                new GrizzlyBears(), new LightningBolt(), new GrizzlyBears(), new LightningBolt(),
                new GrizzlyBears());
        harness.setLibrary(player1, library);

        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(4);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1, 2, 3), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    void upkeepScriesFour() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new LightningBolt(), new GrizzlyBears(), new LightningBolt()));
        harness.forceStep(TurnStep.UPKEEP);

        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(4);
    }

    @Test
    void chaosOffersTopCardForFreeAndKeepsItOnTopWhenDeclined() {
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, new LightningBolt()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    void chaosCanCastTopCreatureWithoutPayingManaCost() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void scryCanReorderBothTopAndBottomCards() {
        List<Card> library = List.of(new GrizzlyBears(), new LightningBolt(),
                new GrizzlyBears(), new LightningBolt(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0), List.of(3, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                library.get(2), library.get(0), library.get(4), library.get(3), library.get(1));
    }

    @Test
    void scryUsesAllCardsInShortLibrary() {
        Card top = new GrizzlyBears();
        Card second = new LightningBolt();
        harness.setLibrary(player1, List.of(top, second));
        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
    }

    @Test
    void chaosCanPlayLandAndConsumesLandPlay() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void chaosCannotPlayLandAfterLandPlayIsUsed() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void chaosCanCastTargetedInstantForFree() {
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void chaosCannotCastSpellWithUnpayableAdditionalCost() {
        Card top = new Fling();
        harness.setLibrary(player1, List.of(top));
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void upkeepScriesForTheNewActivePlayer() {
        Card top = new GrizzlyBears();
        harness.setLibrary(player2, List.of(top));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
    }

    @Test
    void chaosWithEmptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
