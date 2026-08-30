package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stargaze.class, GrizzlyBears.class, Shock.class})
class StargazeTest extends BaseCardTest {

    @Test
    void putsXCardsIntoHandRestIntoGraveyardAndLosesXLife() {
        Card handCard0 = new GrizzlyBears();
        Card graveyardCard0 = new Shock();
        Card handCard1 = new GrizzlyBears();
        Card graveyardCard1 = new Shock();
        setupTopCards(List.of(handCard0, graveyardCard0, handCard1, graveyardCard1));

        harness.setHand(player1, List.of(new Stargaze()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.handleMultipleCardsChosen(player1, List.of(handCard0.getId(), handCard1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(handCard0, handCard1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard0, graveyardCard1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void withZeroXDoesNotMoveCardsOrLoseLife() {
        Card topCard = new GrizzlyBears();
        setupTopCards(List.of(topCard));

        harness.setHand(player1, List.of(new Stargaze()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
