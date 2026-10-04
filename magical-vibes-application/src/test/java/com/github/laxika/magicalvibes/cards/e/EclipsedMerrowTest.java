package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({EclipsedMerrow.class, Plains.class, Island.class, Swamp.class})
class EclipsedMerrowTest extends BaseCardTest {

    @Test
    @CardUsed(MerfolkOfThePearlTrident.class)
    @DisplayName("ETB offers Merfolk, Plains, and Island cards among the top four")
    void etbOffersMatchingCards() {
        MerfolkOfThePearlTrident merfolk = new MerfolkOfThePearlTrident();
        Plains plains = new Plains();
        Island island = new Island();
        harness.setLibrary(player1, List.of(merfolk, new Swamp(), plains, island));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                merfolk.getId(), plains.getId(), island.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and bottoms the rest randomly")
    void choosingMatchingCardPutsItIntoHand() {
        Island island = new Island();
        harness.setLibrary(player1, List.of(island, new Swamp(), new Swamp(), new Swamp()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional reveal bottoms all four cards")
    void decliningBottomsEverything() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains, new Swamp(), new Swamp(), new Swamp()));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no Merfolk, Plains, or Island among the top four, no choice is needed")
    void noMatchingCardNeedsNoChoice() {
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp(), new Swamp(), new Swamp()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }


    @Test
    @DisplayName("Only the top four cards are examined and the rest are bottomed below untouched cards")
    void choosingMerfolkPreservesUntouchedLibraryCards() {
        EclipsedMerrow merfolk = new EclipsedMerrow();
        Swamp first = new Swamp();
        Swamp second = new Swamp();
        Swamp third = new Swamp();
        Plains untouchedPlains = new Plains();
        Island untouchedIsland = new Island();
        harness.setLibrary(player1, List.of(merfolk, first, second, third, untouchedPlains, untouchedIsland));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(merfolk, first, second, third);
        assertThat(choice.validCardIds()).containsExactly(merfolk.getId());

        harness.handleMultipleCardsChosen(player1, List.of(merfolk.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(merfolk);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.subList(0, 2)).containsExactly(untouchedPlains, untouchedIsland);
        assertThat(deck.subList(2, 5)).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library still permits declining the only matching card")
    void shortLibraryCanDeclineOnlyMatchingCard() {
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        harness.setLibrary(player1, List.of(plains, swamp));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(plains, swamp);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, swamp);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With a one-card library, choosing Plains leaves the library empty")
    void choosingOnlyCardInLibrary() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(plains.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a loss")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Eclipsed Merrow");
    }

    @Test
    @DisplayName("The hybrid mana cost can be paid entirely with white mana")
    void canCastWithWhiteMana() {
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.setHand(player1, List.of(new EclipsedMerrow()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));

        harness.assertOnBattlefield(player1, "Eclipsed Merrow");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The choice rejects nonmatching cards and more than one matching card")
    void rejectsInvalidSelectionsWithoutLosingTheChoice() {
        Plains plains = new Plains();
        Island island = new Island();
        Swamp swamp = new Swamp();
        harness.setLibrary(player1, List.of(plains, island, swamp));
        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(swamp.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(plains.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(plains.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, swamp);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new EclipsedMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
