package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneHarvest.class, IronTuskElephant.class, GiantMantis.class, DarkRitual.class})
class BoneHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature cards go on top of the library and the draw is delayed to the next upkeep")
    void chosenCreaturesGoOnTopAndDrawIsDelayed() {
        Card creature = new IronTuskElephant();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new BoneHarvest(), "{2}{B}");

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        assertThat(validIds).containsExactly(creature.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple chosen creature cards are put on top in the chosen order")
    void multipleChosenCreaturesGoOnTopInChosenOrder() {
        Card firstChosen = new IronTuskElephant();
        Card secondChosen = new GiantMantis();
        Card existingTopCard = new DarkRitual();
        harness.setGraveyard(player1, List.of(firstChosen, secondChosen));
        harness.setLibrary(player1, List.of(existingTopCard));
        harness.castFromHand(player1, new BoneHarvest(), "{2}{B}");

        harness.handleMultipleCardsChosen(player1, List.of(firstChosen.getId(), secondChosen.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(firstChosen, secondChosen);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(secondChosen.getId(), firstChosen.getId(), existingTopCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(firstChosen.getId(), secondChosen.getId());
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep, drawing the returned creature")
    void drawResolvesAtNextUpkeep() {
        Card creature = new IronTuskElephant();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new BoneHarvest(), "{2}{B}");
        harness.handleMultipleCardsChosen(player1,
                new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()));
        harness.passBothPriorities();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Only creature cards in your own graveyard are valid targets")
    void onlyOwnCreatureCardsAreValidTargets() {
        Card creature = new IronTuskElephant();
        Card nonCreature = new DarkRitual();
        Card opponentCreature = new GiantMantis();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.castFromHand(player1, new BoneHarvest(), "{2}{B}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Casting with no creature cards in the graveyard still schedules the delayed draw")
    void noCreaturesStillSchedulesDraw() {
        harness.setGraveyard(player1, List.of(new DarkRitual()));
        harness.castFromHand(player1, new BoneHarvest(), "{2}{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Choosing no creature cards leaves them in the graveyard and still schedules the draw")
    void choosingNoCreaturesStillSchedulesDraw() {
        Card creature = new IronTuskElephant();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new BoneHarvest(), "{2}{B}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }
}
