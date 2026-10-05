package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PlatedCrusher;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindRaker.class, PlatedCrusher.class, ScourFromExistence.class})
class MindRakerTest extends BaseCardTest {

    @Test
    void processesAnExiledCardThenEachOpponentDiscards() {
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player2, List.of(new PlatedCrusher()));

        castMindRaker();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Scour from Existence");
        harness.assertInGraveyard(player2, "Plated Crusher");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void decliningExileProcessingDoesNotCauseDiscard() {
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player2, List.of(new PlatedCrusher()));

        castMindRaker();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertInHand(player2, "Plated Crusher");
    }

    @Test
    void doesNotProcessCardsOwnedByTheController() {
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player1, List.of(exiledCard));
        harness.setHand(player2, List.of(new PlatedCrusher()));

        castMindRaker();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertInHand(player2, "Plated Crusher");
    }

    @Test
    void noExiledCardsDoesNotCauseDiscard() {
        harness.setHand(player2, List.of(new PlatedCrusher()));

        castMindRaker();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Plated Crusher");
    }

    @Test
    void canProcessAnOpponentOwnedFaceDownCard() {
        ScourFromExistence exiledCard = new ScourFromExistence();
        gd.addToExile(player2.getId(), exiledCard, null, true);
        harness.setHand(player2, List.of(new PlatedCrusher()));

        castMindRaker();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Scour from Existence");
        harness.assertInGraveyard(player2, "Plated Crusher");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void processingStillSucceedsWhenOpponentHasAnEmptyHand() {
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player2, List.of());

        castMindRaker();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentChoosesExactlyOneCardToDiscard() {
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player2, List.of(new PlatedCrusher(), new MindRaker()));

        castMindRaker();
        harness.setHand(player1, List.of(new PlatedCrusher()));
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));
        harness.handleCardChosen(player2, 1);

        harness.assertInHand(player2, "Plated Crusher");
        harness.assertInGraveyard(player2, "Mind Raker");
        harness.assertInHand(player1, "Plated Crusher");
    }

    private void castMindRaker() {
        harness.castFromHand(player1, new MindRaker(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
