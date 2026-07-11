package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScapeshiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Scapeshift prompts the controller to sacrifice any number of lands")
    void promptsSacrificeChoice() {
        List<Permanent> lands = setupLands(3);
        setupLibraryWithLands();
        castScapeshift();

        harness.passBothPriorities(); // resolve Scapeshift

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(lands.stream().map(Permanent::getId).toList());
    }

    @Test
    @DisplayName("Sacrificing lands offers a library search for that many land cards")
    void sacrificeOffersLandSearch() {
        List<Permanent> lands = setupLands(3);
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();

        // Sacrifice two of the three lands
        harness.handleMultiplePermanentsChosen(player1, List.of(lands.get(0).getId(), lands.get(1).getId()));

        // Two lands sacrificed — one remains
        assertThat(landsOnBattlefield()).hasSize(1);
        // Library search offered, only land cards
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Searched lands enter the battlefield tapped, count equals lands sacrificed")
    void searchedLandsEnterTapped() {
        List<Permanent> lands = setupLands(3);
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(lands.get(0).getId(), lands.get(1).getId()));

        // Pick two land cards from the library
        gs.handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleLibraryCardChosen(gd, player1, 0);

        // One original land plus two fetched lands
        assertThat(landsOnBattlefield()).hasSize(3);
        long tapped = landsOnBattlefield().stream().filter(Permanent::isTapped).count();
        assertThat(tapped).isGreaterThanOrEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search count matches the number of lands sacrificed")
    void searchCountMatchesSacrificed() {
        List<Permanent> lands = setupLands(3);
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();

        // Sacrifice only one land
        harness.handleMultiplePermanentsChosen(player1, List.of(lands.get(0).getId()));

        // Exactly one pick allowed — after one pick the search ends
        gs.handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        long tapped = landsOnBattlefield().stream().filter(Permanent::isTapped).count();
        assertThat(tapped).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing no lands runs no search")
    void sacrificeNoneNoSearch() {
        setupLands(3);
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        // All three lands remain
        assertThat(landsOnBattlefield()).hasSize(3);
    }

    @Test
    @DisplayName("May decline the search after sacrificing (fail to find)")
    void mayDeclineSearch() {
        List<Permanent> lands = setupLands(2);
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(lands.get(0).getId(), lands.get(1).getId()));

        // Decline the search
        gs.handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Lands were sacrificed, none fetched
        assertThat(landsOnBattlefield()).isEmpty();
    }

    @Test
    @DisplayName("With no lands to sacrifice, the spell resolves with no prompt")
    void noLandsNoPrompt() {
        setupLibraryWithLands();
        castScapeshift();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    // ===== Helpers =====

    private List<Permanent> setupLands(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Forest()))
                .toList();
    }

    private void setupLibraryWithLands() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Island(), new Mountain(), new Plains(), new GrizzlyBears()));
    }

    private void castScapeshift() {
        harness.setHand(player1, List.of(new Scapeshift()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
    }

    private List<Permanent> landsOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
    }
}
