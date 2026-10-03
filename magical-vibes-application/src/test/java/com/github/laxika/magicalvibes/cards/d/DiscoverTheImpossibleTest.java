package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BrazenBorrower;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.PlanarIncision;
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

@CardUsed({DiscoverTheImpossible.class, GrizzlyBears.class, Opt.class, PlanarIncision.class, BrazenBorrower.class})
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

    @Test
    @DisplayName("Declining the free cast puts the selected instant into hand")
    void decliningFreeCastPutsCardIntoHand() {
        Card chosenCard = new PlanarIncision();
        castDiscoverTheImpossible(List.of(chosenCard));

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An instant with mana value above two goes into hand without a casting offer")
    void expensiveInstantGoesIntoHand() {
        Card chosenCard = new DiscoverTheImpossible();
        castDiscoverTheImpossible(List.of(chosenCard));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An eligible two-mana instant goes into hand if it has no legal targets")
    void uncastableInstantGoesIntoHand() {
        Card chosenCard = new PlanarIncision();
        castDiscoverTheImpossible(List.of(chosenCard));
        harness.handleCardChosen(player1, 0);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unlooked-at cards stay above the randomly bottomed cards")
    void bottomsOnlyTheUnchosenLookedAtCards() {
        Card chosenCard = new DiscoverTheImpossible();
        Card second = new DiscoverTheImpossible();
        Card third = new DiscoverTheImpossible();
        Card fourth = new DiscoverTheImpossible();
        Card fifth = new DiscoverTheImpossible();
        Card sixth = new DiscoverTheImpossible();
        Card seventh = new DiscoverTheImpossible();
        castDiscoverTheImpossible(List.of(chosenCard, second, third, fourth, fifth, sixth, seventh));

        harness.handleCardChosen(player1, 0);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(library.subList(2, 6)).containsExactlyInAnyOrder(second, third, fourth, fifth);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DiscoverTheImpossible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Discover the Impossible");
    }

    @Test
    @DisplayName("A creature card with an eligible instant Adventure can be offered for a free cast")
    void offersEligibleAdventureDespiteCreatureFrontFace() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card chosenCard = new BrazenBorrower();
        castDiscoverTheImpossible(List.of(chosenCard));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosenCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chosenCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void castDiscoverTheImpossible(List<Card> topCards) {
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new DiscoverTheImpossible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }
}
