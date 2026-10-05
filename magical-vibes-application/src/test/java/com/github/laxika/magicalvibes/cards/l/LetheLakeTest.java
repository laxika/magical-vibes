package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({LetheLake.class, GrizzlyBears.class})
class LetheLakeTest extends BaseCardTest {
    private PlanechaseService planar;

    @BeforeEach
    void setupPlanarState() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new LetheLake(), gd.nextTimestamp()));
    }

    @Test
    void controllerMillsTenCardsAtTheirUpkeep() {
        harness.setLibrary(player1, cards(10));
        harness.forceActivePlayer(player1);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(10);
    }

    @Test
    void chaosMillsTenCardsFromTargetPlayer() {
        harness.setLibrary(player2, cards(10));
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(
                com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
    }

    @Test
    void upkeepMillsTheNewActivePlayerAndOnlyTheTopTenCards() {
        List<Card> library = cards(13);
        List<Card> otherLibrary = cards(12);
        harness.setLibrary(player2, library);
        harness.setLibrary(player1, otherLibrary);
        harness.forceActivePlayer(player2);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library.subList(10, 13));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 10));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(otherLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void chaosCanTargetItsControllerAndMillsOnlyTenCards() {
        List<Card> library = cards(13);
        List<Card> otherLibrary = cards(12);
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, otherLibrary);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(
                com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library.subList(10, 13));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 10));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(otherLibrary);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void chaosMillsAllRemainingCardsWhenTargetLibraryHasFewerThanTen() {
        List<Card> library = cards(3);
        harness.setLibrary(player2, library);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(
                com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void upkeepWithEmptyLibraryDoesNotCauseALoss() {
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new java.util.ArrayList<>();
        for (int index = 0; index < count; index++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
