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

    @Test
    @DisplayName("Can return a noncreature card, but cannot target the discarded card or an opponent's card")
    void returnsNoncreatureFromOwnGraveyardOnly() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card target = new NostalgicDreams();
        Card discarded = new CarrionRats();
        Card opponentsCard = new CentaurVeteran();
        harness.setGraveyard(player1, List.of(target));
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(dreams, discarded));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(), List.of(1));
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreams);
    }

    @Test
    @DisplayName("Cannot use the discard cost to supply a missing graveyard target")
    void cannotCastWithoutEnoughPreexistingTargets() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card discarded = new CarrionRats();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(dreams, discarded));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(
                player1, 0, 1, List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dreams, discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Must choose exactly X distinct targets")
    void requiresExactlyXDistinctTargets() {
        Card first = new CarrionRats();
        Card second = new CentaurVeteran();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new NostalgicDreams(), new CarrionRats(), new CentaurVeteran()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Still returns a remaining legal target and exiles itself when another target leaves")
    void resolvesWithOneRemainingTarget() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card first = new CarrionRats();
        Card second = new CentaurVeteran();
        Card firstDiscard = new CarrionRats();
        Card secondDiscard = new CentaurVeteran();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(dreams, firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of(second, firstDiscard, secondDiscard));
        gd.addToExile(player1.getId(), first);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, dreams);
    }

    @Test
    @DisplayName("Goes to the graveyard instead of exiling itself when all targets become illegal")
    void doesNotExileItselfWhenAllTargetsLeave() {
        NostalgicDreams dreams = new NostalgicDreams();
        Card target = new CarrionRats();
        Card discarded = new CentaurVeteran();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(dreams, discarded));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(), List.of(1));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(discarded));
        gd.addToExile(player1.getId(), target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(discarded, dreams);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }
}
