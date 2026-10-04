package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FlamekinHarbinger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EclipsedFlamekin.class, FlamekinHarbinger.class, GrizzlyBears.class,
        Island.class, Mountain.class, Plains.class})
class EclipsedFlamekinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Elemental, Island, and Mountain cards among the top four")
    void etbOffersMatchingCards() {
        FlamekinHarbinger elemental = new FlamekinHarbinger();
        Island island = new Island();
        Mountain mountain = new Mountain();
        setupTopCards(List.of(elemental, new Plains(), island, mountain));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                elemental.getId(), island.getId(), mountain.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and bottoms the rest randomly")
    void choosingMatchingCardPutsItIntoHand() {
        Island island = new Island();
        setupTopCards(List.of(island, new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional reveal bottoms all four cards")
    void decliningBottomsEverything() {
        Mountain mountain = new Mountain();
        setupTopCards(List.of(mountain, new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no Elemental, Island, or Mountain among the top four, no choice is needed")
    void noMatchingCardNeedsNoChoice() {
        setupTopCards(List.of(new GrizzlyBears(), new Plains(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("An Elemental can be selected and the remaining cards stay below the untouched library")
    void choosingElementalPreservesUntouchedLibrary() {
        EclipsedFlamekin elemental = new EclipsedFlamekin();
        Plains first = new Plains();
        Plains second = new Plains();
        Plains third = new Plains();
        Island untouchedIsland = new Island();
        Mountain untouchedMountain = new Mountain();
        setupTopCards(List.of(elemental, first, second, third, untouchedIsland, untouchedMountain));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(elemental.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elemental);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.subList(0, 2)).containsExactly(untouchedIsland, untouchedMountain);
        assertThat(deck.subList(2, 5)).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Mountain can be chosen from a library with fewer than four cards")
    void choosingMountainFromShortLibrary() {
        Mountain mountain = new Mountain();
        Plains plains = new Plains();
        setupTopCards(List.of(mountain, plains));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(mountain.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The only card in the library can still be declined")
    void decliningOnlyMatchingCard() {
        Island island = new Island();
        setupTopCards(List.of(island));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching card among the top four leaves the deeper cards on top")
    void noMatchDoesNotOfferDeeperCards() {
        Plains first = new Plains();
        Plains second = new Plains();
        Plains third = new Plains();
        Plains fourth = new Plains();
        Island island = new Island();
        Mountain mountain = new Mountain();
        setupTopCards(List.of(first, second, third, fourth, island, mountain));
        castAndResolveEtb();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6);
        assertThat(deck.subList(0, 2)).containsExactly(island, mountain);
        assertThat(deck.subList(2, 6)).containsExactlyInAnyOrder(first, second, third, fourth);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibraryNeedsNoChoice() {
        setupTopCards(List.of());
        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Eclipsed Flamekin");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new EclipsedFlamekin()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
