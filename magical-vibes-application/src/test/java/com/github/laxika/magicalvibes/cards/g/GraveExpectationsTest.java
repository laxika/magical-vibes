package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveExpectations.class, Forest.class, GrizzlyBears.class})
class GraveExpectationsTest extends BaseCardTest {

    @Test
    void heistModeOffersThreeNonlandCardsFromTargetOpponentsLibrary() {
        Card land = new Forest();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, first, second, third));
        prepareSpell();

        harness.castModalInstant(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        PendingInteraction.HeistCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HeistCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3).allMatch(card -> !card.hasType(CardType.LAND));

        Card chosen = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void graveyardModeExilesUpToThreeOpponentCardsAndGainsLife() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second, third));
        prepareSpell();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castModalInstant(player1, 0, 1, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(third);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void graveyardModeCannotTargetYourOwnGraveyard() {
        Card ownCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(ownCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new GraveExpectations()));
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
