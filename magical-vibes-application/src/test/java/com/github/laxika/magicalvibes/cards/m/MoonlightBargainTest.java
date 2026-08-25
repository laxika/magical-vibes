package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonlightBargain.class, GrizzlyBears.class, Shock.class, Forest.class, Island.class,
        LightningBolt.class})
class MoonlightBargainTest extends BaseCardTest {

    @Test
    @DisplayName("Paying for selected cards puts them into hand and the rest into the graveyard")
    void payingForSelectedCards() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new Forest();
        Card card3 = new Island();
        Card card4 = new LightningBolt();
        setTopCards(List.of(card0, card1, card2, card3, card4));

        castMoonlightBargain();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card3.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(card0.getId(), card3.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(card1.getId(), card2.getId(), card4.getId());
    }

    @Test
    @DisplayName("A controller who cannot pay puts all revealed cards into the graveyard")
    void cannotPayForAnyCard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new Forest();
        setTopCards(List.of(card0, card1, card2));
        harness.setLife(player1, 1);

        castMoonlightBargain();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(card0.getId(), card1.getId(), card2.getId());
    }

    @Test
    @DisplayName("A controller cannot choose more cards than they can pay for")
    void cannotOverpayForSelectedCards() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new Forest();
        setTopCards(List.of(card0, card1, card2));
        harness.setLife(player1, 3);

        castMoonlightBargain();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(card0.getId(), card1.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(card0.getId());
    }

    private void castMoonlightBargain() {
        harness.setHand(player1, List.of(new MoonlightBargain()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    private void setTopCards(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
