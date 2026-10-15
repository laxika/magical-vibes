package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShardConvergence.class, Plains.class, Island.class, Swamp.class, Mountain.class,
        GrizzlyBears.class, PsychogenicProbe.class, TempleGarden.class})
class ShardConvergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Finds a Plains, an Island, a Swamp, and a Mountain — one of each into hand")
    void findsOneOfEachBasicLandType() {
        setupAndCast();
        setupLibrary(new Plains(), new Island(), new Swamp(), new Mountain(), new GrizzlyBears());

        harness.passBothPriorities(); // resolve → first (Plains) search prompt

        GameData gd = harness.getGameData();
        // Each restricted subtype search is presented in the order Plains, Island, Swamp, Mountain.
        assertNextSearchIsFor(gd, "Plains");
        harness.handleCardChosen(player1, 0);
        assertNextSearchIsFor(gd, "Island");
        harness.handleCardChosen(player1, 0);
        assertNextSearchIsFor(gd, "Swamp");
        harness.handleCardChosen(player1, 0);
        assertNextSearchIsFor(gd, "Mountain");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Island", "Swamp", "Mountain");
        // The non-matching card stays in the library.
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Found lands are revealed and available lands may be left unfound")
    void searchesAreRestricted() {
        setupAndCast();
        setupLibrary(new Plains(), new Island(), new Swamp(), new Mountain());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Swamp");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Island", "Mountain");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals") && entry.contains("Plains"))
                .anyMatch(entry -> entry.contains("reveals") && entry.contains("Swamp"));
        harness.assertInGraveyard(player1, "Shard Convergence");
    }

    @Test
    @DisplayName("A basic land type absent from the library is skipped; the others are still found")
    void absentSubtypeIsSkipped() {
        setupAndCast();
        setupLibrary(new Plains(), new Swamp(), new Mountain()); // no Island

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertNextSearchIsFor(gd, "Plains");
        harness.handleCardChosen(player1, 0);
        // The Island search auto-resolves as "no match" and resolution continues straight to Swamp.
        assertNextSearchIsFor(gd, "Swamp");
        harness.handleCardChosen(player1, 0);
        assertNextSearchIsFor(gd, "Mountain");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Swamp", "Mountain");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("finds no Island cards"));
    }

    @Test
    @DisplayName("Empty library finds nothing and the spell still resolves to the graveyard")
    void emptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shard Convergence");
    }

    @Test
    @DisplayName("Searching for all four lands causes only one shuffle")
    void shufflesOnlyOnceAfterFindingAllLands() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        setupAndCast();
        setupLibrary(new Plains(), new Island(), new Swamp(), new Mountain());

        harness.passBothPriorities();
        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, 0);
        }
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shard Convergence");
    }

    @Test
    @DisplayName("An empty library is still shuffled exactly once")
    void emptyLibraryStillShufflesOnlyOnce() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Shard Convergence");
    }

    @Test
    @DisplayName("All available lands may be left unfound")
    void mayFindNoCardsDespiteAvailableMatches() {
        setupAndCast();
        setupLibrary(new Plains(), new Island(), new Swamp(), new Mountain());

        harness.passBothPriorities();
        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, -1);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Island", "Swamp", "Mountain");
        harness.assertInGraveyard(player1, "Shard Convergence");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new ShardConvergence()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {3}{G}
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void assertNextSearchIsFor(GameData gd, String subtypeName) {
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).as("expected a library search for %s", subtypeName).isNotNull();
        assertThat(search.params().cards().stream().map(Card::getName))
                .as("search should only present %s cards", subtypeName)
                .containsExactly(subtypeName);
    }

    @Test
    @DisplayName("A nonbasic Plains card can be found without entering the battlefield")
    void findsNonbasicPlainsCard() {
        TempleGarden garden = new TempleGarden();
        setupAndCast();
        setupLibrary(garden);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(garden);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Temple Garden");
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Shard Convergence");
    }
}
