package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiabolicRevelation.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class, Shock.class})
class DiabolicRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Offers every card in the library, up to X picks")
    void offersAnyCardUpToX() {
        castRevelation(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(offeredNames()).containsExactlyInAnyOrder(
                "Grizzly Bears", "Llanowar Elves", "Air Elemental", "Shock");
        assertThat(activeSearch().params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(activeSearch().params().remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chosen cards go to hand and the rest stay in the library")
    void chosenCardsGoToHand() {
        castRevelation(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, indexOf("Shock"));
        harness.handleCardChosen(player1, indexOf("Air Elemental"));

        harness.assertInHand(player1, "Shock");
        harness.assertInHand(player1, "Air Elemental");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("X=0 searches nothing and the spell goes to the graveyard")
    void xZeroFindsNothing() {
        castRevelation(0);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Diabolic Revelation");
    }

    @Test
    @DisplayName("May choose no cards even when X is positive")
    void mayChooseNoCards() {
        castRevelation(2);
        setupLibrary();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Diabolic Revelation");
    }

    @Test
    @DisplayName("May stop searching after choosing fewer than X cards")
    void mayStopAfterOneCard() {
        castRevelation(3);
        setupLibrary();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, indexOf("Shock"));
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Shock");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(3);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Diabolic Revelation");
    }

    @Test
    @DisplayName("Completes when the library contains fewer than X cards")
    void librarySmallerThanX() {
        castRevelation(3);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, indexOf("Shock"));

        harness.assertInHand(player1, "Shock");
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Diabolic Revelation");
    }

    @Test
    @DisplayName("Resolves with an empty library")
    void emptyLibrary() {
        castRevelation(2);
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Diabolic Revelation");
    }

    private void castRevelation(int xValue) {
        harness.setHand(player1, List.of(new DiabolicRevelation()));
        harness.addMana(player1, ManaColor.BLACK, xValue + 5);
        harness.castSorcery(player1, 0, xValue);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new AirElemental(), new Shock()));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private List<String> offeredNames() {
        return activeSearch().params().cards().stream().map(Card::getName).toList();
    }

    private int indexOf(String name) {
        int index = offeredNames().indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        return index;
    }
}
