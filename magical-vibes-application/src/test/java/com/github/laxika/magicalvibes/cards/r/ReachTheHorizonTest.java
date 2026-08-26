package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReachTheHorizon.class, Forest.class, Plains.class, GrizzlyBears.class})
class ReachTheHorizonTest extends BaseCardTest {

    @Test
    @DisplayName("Offers basic lands and Towns, but not other cards")
    void offersBasicLandsAndTowns() {
        setLibrary(new Forest(), new Plains(), town("Town One"), new GrizzlyBears());
        castReachTheHorizon();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().requireDifferentNames()).isTrue();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Plains", "Town One");
    }

    @Test
    @DisplayName("Requires different names and puts chosen cards onto the battlefield tapped")
    void requiresDifferentNamesAndEntersTapped() {
        Card firstTown = town("Town One");
        Card duplicateTown = town("Town One");
        Card secondTown = town("Town Two");
        setLibrary(firstTown, duplicateTown, secondTown, new Forest(), new GrizzlyBears());
        castReachTheHorizon();

        harness.passBothPriorities();

        int firstTownIndex = offeredIndex(firstTown.getName());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(firstTownIndex));

        assertThat(activeSearch().params().cards())
                .noneMatch(card -> card.getName().equals(firstTown.getName()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND)
                        || permanent.getCard().getSubtypes().contains(CardSubtype.TOWN))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card == firstTown || card == secondTown);
    }

    private void castReachTheHorizon() {
        harness.setHand(player1, List.of(new ReachTheHorizon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private int offeredIndex(String name) {
        List<Card> offeredCards = activeSearch().params().cards();
        int index = 0;
        while (!offeredCards.get(index).getName().equals(name)) {
            index++;
        }
        return index;
    }

    private Card town(String name) {
        Card town = new Card();
        town.setName(name);
        town.setType(CardType.LAND);
        town.setSubtypes(List.of(CardSubtype.TOWN));
        return town;
    }
}
