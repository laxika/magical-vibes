package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomsdayExcruciator.class, BeaconOfUnrest.class, Forest.class, GrizzlyBears.class})
class DoomsdayExcruciatorTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, exiles all but the bottom six cards of each library face down")
    void castExilesAllButBottomSixFromEachLibraryFaceDown() {
        List<Card> player1Exiled = cards(4);
        List<Card> player1Bottom = cards(6);
        List<Card> player2Exiled = cards(3);
        List<Card> player2Bottom = cards(6);
        harness.setLibrary(player1, concatenate(player1Exiled, player1Bottom));
        harness.setLibrary(player2, concatenate(player2Exiled, player2Bottom));
        harness.setHand(player1, List.of(new DoomsdayExcruciator()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(player1Bottom);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(player2Bottom);
        assertFaceDownExile(player1, player1Exiled);
        assertFaceDownExile(player2, player2Exiled);
    }

    @Test
    @DisplayName("Libraries with six or fewer cards are left unchanged")
    void doesNotExileWhenLibraryHasSixOrFewerCards() {
        List<Card> player1Library = cards(6);
        List<Card> player2Library = cards(5);
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        harness.setHand(player1, List.of(new DoomsdayExcruciator()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(player1Library);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(player2Library);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Putting it onto the battlefield without casting it does not exile libraries")
    void enteringWithoutBeingCastDoesNotExileLibraries() {
        List<Card> player1Library = cards(7);
        List<Card> player2Library = cards(7);
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        harness.setGraveyard(player1, List.of(new DoomsdayExcruciator()));
        BeaconOfUnrest beacon = new BeaconOfUnrest();
        harness.setHand(player1, List.of(beacon));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(concatenate(player1Library, List.of(beacon)));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(player2Library);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("At the beginning of its controller's upkeep, it draws a card")
    void drawsAtControllerUpkeep() {
        Card drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new DoomsdayExcruciator());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        harness.assertInHand(player1, "Forest");
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> (Card) new GrizzlyBears())
                .toList();
    }

    private List<Card> concatenate(List<Card> first, List<Card> second) {
        return java.util.stream.Stream.concat(first.stream(), second.stream()).toList();
    }

    private void assertFaceDownExile(com.github.laxika.magicalvibes.model.Player player,
                                     List<Card> expectedCards) {
        assertThat(gd.exiledCards)
                .filteredOn(entry -> entry.ownerId().equals(player.getId()))
                .hasSize(expectedCards.size())
                .allMatch(ExiledCardEntry::faceDown)
                .extracting(ExiledCardEntry::card)
                .containsExactlyElementsOf(expectedCards);
    }
}
