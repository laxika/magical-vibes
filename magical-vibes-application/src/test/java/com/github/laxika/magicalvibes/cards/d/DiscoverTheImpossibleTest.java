package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscoverTheImpossible.class, GrizzlyBears.class, Opt.class})
class DiscoverTheImpossibleTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at the top five and puts the unchosen cards on the bottom randomly")
    void exilesChosenCardFaceDownAndBottomsTheRest() {
        Card chosenCard = new GrizzlyBears();
        List<Card> topCards = List.of(chosenCard, new Opt(), new GrizzlyBears(), new Opt(), new GrizzlyBears());
        castDiscoverTheImpossible(topCards);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards.subList(1, 5));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(chosenCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Offers an eligible instant to cast for free or put into hand")
    void eligibleInstantCanBeCastForFree() {
        Card chosenCard = new Opt();
        List<Card> topCards = List.of(chosenCard, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        castDiscoverTheImpossible(topCards);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosenCard);
        assertThat(gd.exiledCards).filteredOn(entry -> entry.card().getId().equals(chosenCard.getId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(chosenCard);
    }

    private void castDiscoverTheImpossible(List<Card> topCards) {
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new DiscoverTheImpossible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }
}
