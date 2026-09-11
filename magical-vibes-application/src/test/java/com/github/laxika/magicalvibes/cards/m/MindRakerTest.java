package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindRaker.class, Forest.class, PathToExile.class})
class MindRakerTest extends BaseCardTest {

    @Test
    void processesAnExiledCardThenEachOpponentDiscards() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player2, List.of(new Forest()));

        castMindRaker();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Path to Exile");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void decliningExileProcessingDoesNotCauseDiscard() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player2, List.of(new Forest()));

        castMindRaker();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertInHand(player2, "Forest");
    }

    @Test
    void doesNotProcessCardsOwnedByTheController() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player1, List.of(exiledCard));
        harness.setHand(player2, List.of(new Forest()));

        castMindRaker();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertInHand(player2, "Forest");
    }

    private void castMindRaker() {
        harness.setHand(player1, List.of(new MindRaker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
