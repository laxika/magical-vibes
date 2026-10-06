package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalambTRexaur;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GongagaReactorTown;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StartingTown;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReachTheHorizon.class, Forest.class, Plains.class, GrizzlyBears.class,
        StartingTown.class, GongagaReactorTown.class, BalambTRexaur.class})
class ReachTheHorizonTest extends BaseCardTest {

    @Test
    @DisplayName("Offers basic lands and Towns, but not other cards")
    void offersBasicLandsAndTowns() {
        setLibrary(new Forest(), new Plains(), town("Town One"), new GrizzlyBears());
        castReachTheHorizon();

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

        int firstTownIndex = offeredIndex(firstTown.getName());
        harness.handleCardChosen(player1, firstTownIndex);

        assertThat(activeSearch().params().cards())
                .noneMatch(card -> card.getName().equals(firstTown.getName()));
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND)
                        || permanent.getCard().getSubtypes().contains(CardSubtype.TOWN))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card == firstTown || card == secondTown);
    }

    @Test
    @DisplayName("Can choose zero cards even when eligible cards are available")
    void canChooseZeroCards() {
        Forest forest = new Forest();
        StartingTown town = new StartingTown();
        setLibrary(forest, town);
        castReachTheHorizon();

        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, town);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Starting Town");
        harness.assertInGraveyard(player1, "Reach the Horizon");
    }

    @Test
    @DisplayName("Can stop after one card and still put it onto the battlefield tapped")
    void canChooseOnlyOneCard() {
        StartingTown town = new StartingTown();
        Forest forest = new Forest();
        setLibrary(town, forest);
        castReachTheHorizon();

        harness.handleCardChosen(player1, offeredIndex("Starting Town"));
        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == town)
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player1, "Reach the Horizon");
    }

    @Test
    @DisplayName("Can find two real Town cards and both enter tapped")
    void findsTwoTowns() {
        StartingTown firstTown = new StartingTown();
        GongagaReactorTown secondTown = new GongagaReactorTown();
        BalambTRexaur creature = new BalambTRexaur();
        setLibrary(firstTown, secondTown, creature);
        castReachTheHorizon();

        assertThat(activeSearch().params().cards()).containsExactly(firstTown, secondTown);
        harness.handleCardChosen(player1, offeredIndex("Starting Town"));
        harness.assertNotOnBattlefield(player1, "Starting Town");
        harness.handleCardChosen(player1, offeredIndex("Gongaga, Reactor Town"));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == firstTown || permanent.getCard() == secondTown)
                .hasSize(2).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player2, "Starting Town");
        harness.assertNotOnBattlefield(player2, "Gongaga, Reactor Town");
    }

    @Test
    @DisplayName("Duplicate basic land names leave only one eligible selection")
    void duplicateBasicsYieldOnlyOneLand() {
        Forest first = new Forest();
        Forest duplicate = new Forest();
        setLibrary(first, duplicate);
        castReachTheHorizon();

        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == first)
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(duplicate);
        harness.assertInGraveyard(player1, "Reach the Horizon");
    }

    @Test
    @DisplayName("Resolves without finding anything in a library with no eligible cards")
    void noEligibleCards() {
        BalambTRexaur creature = new BalambTRexaur();
        setLibrary(creature);
        castReachTheHorizon();

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Balamb T-Rexaur");
        harness.assertInGraveyard(player1, "Reach the Horizon");
    }

    @Test
    @DisplayName("Resolves when the library is empty")
    void emptyLibrary() {
        setLibrary();
        castReachTheHorizon();

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Reach the Horizon");
    }

    @Test
    @DisplayName("Can find a basic land and a Town together")
    void findsBasicLandAndTownTogether() {
        Forest forest = new Forest();
        StartingTown town = new StartingTown();
        setLibrary(forest, town);
        castReachTheHorizon();

        harness.handleCardChosen(player1, offeredIndex("Forest"));
        harness.handleCardChosen(player1, offeredIndex("Starting Town"));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == forest || permanent.getCard() == town)
                .hasSize(2).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Reach the Horizon");
    }

    @Test
    @DisplayName("Can find two differently named basic lands")
    void findsTwoBasicLands() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        setLibrary(forest, plains);
        castReachTheHorizon();

        harness.handleCardChosen(player1, offeredIndex("Forest"));
        harness.handleCardChosen(player1, offeredIndex("Plains"));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == forest || permanent.getCard() == plains)
                .hasSize(2).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Reach the Horizon");
    }

    private void castReachTheHorizon() {
        harness.setHand(player1, List.of(new ReachTheHorizon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
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
