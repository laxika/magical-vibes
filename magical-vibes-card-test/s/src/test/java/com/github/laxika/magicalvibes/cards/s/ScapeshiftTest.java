package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TheGitrogMonster;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scapeshift.class, Forest.class, Island.class, Mountain.class, Plains.class,
        GreenwoodSentinel.class, CosisTrickster.class, TheGitrogMonster.class, AshayaSoulOfTheWild.class})
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
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

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
        harness.handleCardChosen(player1, 0);
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
        harness.handleCardChosen(player1, -1);

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

    @Test
    @DisplayName("Only the controller's lands can be chosen for sacrifice")
    void excludesOpposingLandsAndNonlands() {
        List<Permanent> lands = setupLands(2);
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new Forest());
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactlyInAnyOrderElementsOf(lands.stream().map(Permanent::getId).toList());
    }

    @Test
    @DisplayName("May stop after finding fewer lands than were sacrificed")
    void mayFindFewerLands() {
        List<Permanent> lands = setupLands(3);
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, lands.stream().map(Permanent::getId).toList());

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(landsOnBattlefield()).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsAll(lands.stream().map(Permanent::getCard).toList());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding the only land completes a search with a larger allowance")
    void fewerAvailableLandsThanSacrificed() {
        List<Permanent> lands = setupLands(3);
        harness.setLibrary(player1, List.of(new Island(), new GreenwoodSentinel()));
        castScapeshift();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, lands.stream().map(Permanent::getId).toList());

        harness.handleCardChosen(player1, 0);

        assertThat(landsOnBattlefield()).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card instanceof GreenwoodSentinel);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing zero lands still shuffles the library")
    void sacrificingZeroStillShuffles() {
        setupLands(2);
        setupLibraryWithLands();
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        castScapeshift();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertShuffleTrigger(trickster);
        assertThat(landsOnBattlefield()).hasSize(2);
    }

    @Test
    @DisplayName("Controlling no lands still shuffles the library")
    void controllingNoLandsStillShuffles() {
        setupLibraryWithLands();
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        castScapeshift();

        harness.passBothPriorities();

        assertShuffleTrigger(trickster);
    }

    @Test
    @DisplayName("Searching an empty library still causes a shuffle")
    void emptyLibraryStillShuffles() {
        List<Permanent> lands = setupLands(1);
        harness.setLibrary(player1, List.of());
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        castScapeshift();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(lands.getFirst().getId()));

        assertShuffleTrigger(trickster);
        assertThat(landsOnBattlefield()).isEmpty();
    }

    @Test
    @DisplayName("Finding no cards still shuffles once")
    void failingToFindStillShuffles() {
        List<Permanent> lands = setupLands(2);
        setupLibraryWithLands();
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        castScapeshift();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, lands.stream().map(Permanent::getId).toList());

        harness.handleCardChosen(player1, -1);

        assertShuffleTrigger(trickster);
    }

    @Test
    @DisplayName("Sacrificed lands reach the graveyard simultaneously and trigger Gitrog once")
    void multipleSacrificedLandsTriggerGitrogOnce() {
        List<Permanent> lands = setupLands(3);
        harness.addToBattlefield(player1, new TheGitrogMonster());
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, lands.stream().map(Permanent::getId).toList());

        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsAll(lands.stream().map(Permanent::getCard).toList());
    }

    @Test
    @DisplayName("Fetched lands enter as a group and the completed search shuffles once")
    void fetchedLandsEnterTogetherAndShuffle() {
        List<Permanent> lands = setupLands(2);
        setupLibraryWithLands();
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        castScapeshift();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, lands.stream().map(Permanent::getId).toList());

        harness.handleCardChosen(player1, 0);
        assertThat(landsOnBattlefield()).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(landsOnBattlefield()).hasSize(2).allMatch(Permanent::isTapped);
        assertShuffleTrigger(trickster);
    }

    @Test
    @DisplayName("Creatures made into lands by Ashaya can be sacrificed")
    void maySacrificeCreaturesThatAreCurrentlyLands() {
        List<Permanent> lands = setupLands(1);
        Permanent ashaya = harness.addToBattlefieldAndReturn(player1, new AshayaSoulOfTheWild());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        setupLibraryWithLands();
        castScapeshift();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(lands.getFirst().getId(), ashaya.getId(), sentinel.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(sentinel.getId()));
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(landsOnBattlefield()).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void assertShuffleTrigger(Permanent trickster) {
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> setupLands(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Forest()))
                .toList();
    }

    private void setupLibraryWithLands() {
        harness.setLibrary(player1, List.of(new Island(), new Mountain(), new Plains(), new GreenwoodSentinel()));
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
