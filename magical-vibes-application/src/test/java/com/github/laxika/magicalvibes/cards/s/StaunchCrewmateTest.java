package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FathomFleetBoarder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaunchCrewmate.class, FathomFleetBoarder.class, GrizzlyBears.class,
        Island.class, Ornithopter.class})
class StaunchCrewmateTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers artifact and Pirate cards among the top four")
    void etbOffersArtifactAndPirate() {
        Card artifact = new Ornithopter();
        Card pirate = new FathomFleetBoarder();
        setupTopCards(List.of(artifact, new GrizzlyBears(), pirate, new Island()));

        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), pirate.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Choosing a Pirate puts it into hand and bottoms the other cards")
    void choosingPiratePutsItIntoHand() {
        Card pirate = new FathomFleetBoarder();
        Card artifact = new Ornithopter();
        List<Card> topCards = List.of(new GrizzlyBears(), pirate, artifact, new Island());
        setupTopCards(topCards);

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(pirate.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(pirate);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(topCards.get(0), topCards.get(2), topCards.get(3));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining puts all four cards on the library bottom")
    void mayDecline() {
        List<Card> topCards = List.of(new GrizzlyBears(), new Island(), new Ornithopter(),
                new FathomFleetBoarder());
        setupTopCards(topCards);

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no artifact or Pirate among the top four, all cards go to the library bottom")
    void noMatchingCardReordersDirectly() {
        List<Card> topCards = List.of(new GrizzlyBears(), new Island(), new GrizzlyBears(), new Island());
        setupTopCards(topCards);

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    @DisplayName("Choosing an artifact leaves untouched cards above the bottomed cards")
    void choosingArtifactPreservesUnlookedCards() {
        Card artifact = new Ornithopter();
        List<Card> topCards = List.of(new Island(), artifact, new StaunchCrewmate(), new Island());
        Card fifth = new Island();
        Card sixth = new StaunchCrewmate();
        harness.setLibrary(player1, List.of(topCards.get(0), artifact, topCards.get(2),
                topCards.get(3), fifth, sixth));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 5))
                .containsExactlyInAnyOrder(topCards.get(0), topCards.get(2), topCards.get(3));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A sole eligible card in a short library may still be declined")
    void mayDeclineSoleEligibleCardInShortLibrary() {
        Card pirate = new StaunchCrewmate();
        Card land = new Island();
        harness.setLibrary(player1, List.of(pirate, land));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(pirate, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The only card in the library can be selected if it is a Pirate")
    void selectsPirateFromOneCardLibrary() {
        Card pirate = new StaunchCrewmate();
        harness.setLibrary(player1, List.of(pirate));

        castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of(pirate.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pirate);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library needs no choice and causes no life loss")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new StaunchCrewmate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
