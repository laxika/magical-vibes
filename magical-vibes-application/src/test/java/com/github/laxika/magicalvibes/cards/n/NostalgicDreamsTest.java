package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CarrionRats;
import com.github.laxika.magicalvibes.cards.c.CentaurVeteran;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NostalgicDreams.class, CarrionRats.class, CentaurVeteran.class})
class NostalgicDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Discards X cards, returns X target graveyard cards, and exiles itself")
    void discardsReturnsAndExilesItself() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card firstCard = new CarrionRats();
        Card secondCard = new CentaurVeteran();
        Card firstDiscard = new CarrionRats();
        Card secondDiscard = new CentaurVeteran();
        harness.setGraveyard(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(dreams, firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(firstCard.getId(), secondCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstCard.getId(), secondCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCard, secondCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreams);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Carrion Rats", "Centaur Veteran");
    }

    @Test
    @DisplayName("X=0 returns no cards and still exiles itself")
    void xZeroReturnsNoCardsAndExilesItself() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card graveyardCard = new CarrionRats();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(dreams));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreams);
    }

    @Test
    @DisplayName("Cannot cast X=2 without two cards to discard")
    void cannotCastWithoutEnoughCardsToDiscard() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card firstGraveyardCard = new CarrionRats();
        Card secondGraveyardCard = new CentaurVeteran();
        Card onlyDiscard = new CarrionRats();
        harness.setGraveyard(player1, List.of(firstGraveyardCard, secondGraveyardCard));
        harness.setHand(player1, List.of(dreams, onlyDiscard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 2,
                List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard 2 cards");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dreams, onlyDiscard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstGraveyardCard, secondGraveyardCard);
    }
}
