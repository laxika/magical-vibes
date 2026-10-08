package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VodaSeaScavenger.class, Plains.class, Island.class, Swamp.class})
class VodaSeaScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets you put one of the Domain cards on top and randomly bottoms the rest")
    void etbPutsChosenCardOnTopAndRestOnBottom() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        Card first = new VodaSeaScavenger();
        Card chosen = new VodaSeaScavenger();
        Card last = new VodaSeaScavenger();
        castAndResolve(first, chosen, last);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, last, chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("You may decline and all looked-at cards go to the bottom")
    void mayDecline() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card first = new VodaSeaScavenger();
        Card last = new VodaSeaScavenger();
        castAndResolve(first, last);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, last);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Domain counts duplicate basic land types only once")
    void duplicateBasicLandTypesCountOnce() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card first = new VodaSeaScavenger();
        Card second = new VodaSeaScavenger();
        Card belowDomainCount = new VodaSeaScavenger();
        castAndResolve(first, second, belowDomainCount);

        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(first, second);
        assertThat(search.params().cards()).doesNotContain(belowDomainCount);
    }

    @Test
    void choiceDoesNotAskToRevealPrivateCards() {
        harness.addToBattlefield(player1, new Island());
        castAndResolve(new VodaSeaScavenger(), new Plains());

        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.messagePrompt()).doesNotContainIgnoringCase("reveal");
        harness.handleCardChosen(player1, 0);
        assertThat(gameLogContains("reveals")).isFalse();
    }

    @Test
    void zeroDomainLeavesLibraryUnchangedAndIgnoresOpponentsLands() {
        harness.addToBattlefield(player2, new Island());
        Card first = new VodaSeaScavenger();
        Card second = new Plains();
        castAndResolve(first, second);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        harness.addToBattlefield(player1, new Island());
        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayBottomTheOnlyLookedAtCardWithoutShufflingUnlookedCards() {
        harness.addToBattlefield(player1, new Island());
        Card lookedAt = new VodaSeaScavenger();
        Card untouchedFirst = new Plains();
        Card untouchedSecond = new Swamp();
        castAndResolve(lookedAt, untouchedFirst, untouchedSecond);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouchedFirst, untouchedSecond, lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosingOneKeepsUnlookedCardsBetweenItAndTheBottomedCards() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card bottomed = new Swamp();
        Card chosen = new VodaSeaScavenger();
        Card untouchedFirst = new Plains();
        Card untouchedSecond = new Island();
        castAndResolve(bottomed, chosen, untouchedFirst, untouchedSecond);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(chosen, untouchedFirst, untouchedSecond, bottomed);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void domainIsDeterminedWhenTheTriggerResolves() {
        harness.addToBattlefield(player1, new Island());
        Card first = new VodaSeaScavenger();
        Card second = new Plains();
        Card third = new Swamp();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new VodaSeaScavenger(), "{2}{U}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(first, second);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, third, second);
    }

    @Test
    void shortLibraryLooksAtAllAvailableCards() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        Card onlyCard = new VodaSeaScavenger();
        castAndResolve(onlyCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(onlyCard);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolve(Card... libraryCards) {
        harness.setLibrary(player1, List.of(libraryCards));
        harness.castFromHand(player1, new VodaSeaScavenger(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
