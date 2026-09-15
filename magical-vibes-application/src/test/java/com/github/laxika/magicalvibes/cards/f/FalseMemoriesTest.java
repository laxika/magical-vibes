package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.Overmaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalseMemories.class, Overmaster.class})
class FalseMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Mills seven cards and exiles seven cards at the next end step")
    void millsThenExilesSevenCardsAtNextEndStep() {
        harness.setLibrary(player1, overmasters(7));
        castAndResolveFalseMemories();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
        assertThat(gd.exiledCards).isEmpty();

        resolveAtNextEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        for (int i = 0; i < 7; i++) {
            harness.handleGraveyardCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Exiles all remaining graveyard cards when fewer than seven remain")
    void exilesFewerThanSevenRemainingCards() {
        harness.setLibrary(player1, overmasters(3));
        castAndResolveFalseMemories();
        resolveAtNextEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Exiles cards added to your graveyard before the delayed trigger resolves")
    void includesCardsAddedBeforeDelayedTriggerResolves() {
        harness.setLibrary(player1, overmasters(7));
        castAndResolveFalseMemories();

        Overmaster addedBeforeEndStep = new Overmaster();
        List<Card> graveyard = new ArrayList<>(gd.playerGraveyards.get(player1.getId()));
        graveyard.add(addedBeforeEndStep);
        harness.setGraveyard(player1, graveyard);

        resolveAtNextEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        int addedCardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(addedBeforeEndStep);
        harness.handleGraveyardCardChosen(player1, addedCardIndex);
        for (int i = 0; i < 6; i++) {
            harness.handleGraveyardCardChosen(player1, 0);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(addedBeforeEndStep).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Waits for the following end step when cast during an end step")
    void waitsForFollowingEndStepWhenCastDuringEndStep() {
        harness.setLibrary(player1, overmasters(7));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        FalseMemories spell = new FalseMemories();
        harness.castFromHand(player1, spell, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        for (int i = 0; i < 7; i++) {
            harness.handleGraveyardCardChosen(player1, 0);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void resolveAtNextEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castAndResolveFalseMemories() {
        harness.castFromHand(player1, new FalseMemories(), "{1}{U}");
        harness.passBothPriorities();
    }

    private List<Card> overmasters(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Overmaster());
        }
        return cards;
    }
}
