package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerrowHarbinger.class, MerrowCommerce.class, DeeptreadMerrow.class, GoldmeadowStalwart.class, Plains.class, Island.class})
class MerrowHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Merrow Harbinger creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Merrow Harbinger");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability only offers Merfolk cards")
    void acceptingMayOffersOnlyMerfolk() {
        setupAndCast();
        setupLibraryWithMerfolk();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.MERFOLK));
    }

    @Test
    @DisplayName("Choosing a Merfolk card puts it on top of the library")
    void choosingMerfolkPutsItOnTop() {
        setupAndCast();
        setupLibraryWithMerfolk();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithMerfolk();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability with no Merfolk finds no card")
    void acceptingMayWithNoMerfolkFindsNoCard() {
        setupAndCast();
        setupLibraryWithoutMerfolk();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Goldmeadow Stalwart", "Plains", "Island");
    }

    @Test
    @DisplayName("The search can find a noncreature Merfolk card")
    void findsNoncreatureMerfolk() {
        setupAndCast();
        Card commerce = new MerrowCommerce();
        Card island = new Island();
        harness.setLibrary(player1, List.of(island, commerce));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactly(commerce, island);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may fail to find even when a Merfolk is available")
    void mayFailToFindAvailableMerfolk() {
        setupAndCast();
        Card merrow = new DeeptreadMerrow();
        Card island = new Island();
        harness.setLibrary(player1, List.of(merrow, island));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(merrow, island);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library completes without a card choice")
    void searchingEmptyLibraryCompletes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Merrow Harbinger");
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new MerrowHarbinger(), "{3}{U}");
    }

    private void setupLibraryWithMerfolk() {
        harness.setLibrary(player1, List.of(new DeeptreadMerrow(), new GoldmeadowStalwart(), new Island()));
    }

    private void setupLibraryWithoutMerfolk() {
        harness.setLibrary(player1, List.of(new GoldmeadowStalwart(), new Plains(), new Island()));
    }
}
