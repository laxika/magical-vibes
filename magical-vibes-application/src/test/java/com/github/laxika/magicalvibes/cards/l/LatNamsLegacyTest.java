package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LatNamsLegacyTest extends BaseCardTest {

    private Card castLatNamsLegacy(List<Card> otherHandCards) {
        Card legacy = new LatNamsLegacy();
        harness.setHand(player1, java.util.stream.Stream.concat(
                java.util.stream.Stream.of(legacy), otherHandCards.stream()).toList());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        return legacy;
    }

    @Test
    @DisplayName("Chosen hand card is shuffled into the library and the delayed draw is scheduled")
    void shufflesChosenCardAndSchedulesDraw() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        castLatNamsLegacy(List.of(bears, shock));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore + 1).contains(bears);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(2);
    }

    @Test
    @DisplayName("The scheduled draw of two cards resolves at the next upkeep")
    void drawsTwoAtNextUpkeep() {
        Card bears = new GrizzlyBears();
        castLatNamsLegacy(List.of(bears));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("With an empty hand nothing is shuffled and no draw is scheduled")
    void emptyHandDoesNothing() {
        castLatNamsLegacy(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
