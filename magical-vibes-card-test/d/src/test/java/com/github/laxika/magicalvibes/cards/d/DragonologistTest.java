package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dragonologist.class, Divination.class, GrizzlyBears.class, Island.class, ShivanDragon.class, Shock.class})
class DragonologistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers instant, sorcery, and Dragon cards among the top six")
    void etbOffersMatchingCards() {
        Card shock = new Shock();
        Card divination = new Divination();
        Card dragon = new ShivanDragon();
        setupTopCards(List.of(shock, new GrizzlyBears(), divination, new Island(), dragon, new GrizzlyBears()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), divination.getId(), dragon.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Untapped Dragons you control have hexproof")
    void grantsHexproofToOwnUntappedDragons() {
        harness.addToBattlefield(player1, new Dragonologist());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentDragon, Keyword.HEXPROOF)).isFalse();

        dragon.tap();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isFalse();

        dragon.untap();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void selectedCardGoesToHandAndOnlyLookedAtCardsGoToBottom() {
        Card selected = new Shock();
        List<Card> remaining = List.of(new GrizzlyBears(), new Divination(), new Island(),
                new ShivanDragon(), new GrizzlyBears());
        Card untouchedTop = new Island();
        Card untouchedSecond = new Shock();
        setupTopCards(List.of(selected, remaining.get(0), remaining.get(1), remaining.get(2),
                remaining.get(3), remaining.get(4), untouchedTop, untouchedSecond));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(7);
        assertThat(library.subList(0, 2)).containsExactly(untouchedTop, untouchedSecond);
        assertThat(library.subList(2, 7)).containsExactlyInAnyOrderElementsOf(remaining);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclineEvenWhenShortLibraryHasOneEligibleCard() {
        Card eligible = new ShivanDragon();
        Card other = new Island();
        setupTopCards(List.of(eligible, other));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(eligible, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noEligibleCardsAreAllPutBelowUnexaminedCards() {
        List<Card> lookedAt = List.of(new Island(), new GrizzlyBears(), new Island(),
                new GrizzlyBears(), new Island(), new GrizzlyBears());
        Card untouched = new Shock();
        setupTopCards(List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), lookedAt.get(5), untouched));
        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(7);
        assertThat(library.getFirst()).isSameAs(untouched);
        assertThat(library.subList(1, 7)).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        setupTopCards(List.of());
        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotGrantHexproofToNonDragons() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Dragonologist());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, source, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void tappingDragonologistDoesNotRemoveHexproofFromUntappedDragons() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Dragonologist());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        source.tap();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.castFromHand(player1, new Dragonologist(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
