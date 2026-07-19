package com.github.laxika.magicalvibes.cards.s;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        pickFirstMatch(gd);
        assertNextSearchIsFor(gd, "Island");
        pickFirstMatch(gd);
        assertNextSearchIsFor(gd, "Swamp");
        pickFirstMatch(gd);
        assertNextSearchIsFor(gd, "Mountain");
        pickFirstMatch(gd);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Island", "Swamp", "Mountain");
        // The non-matching card stays in the library.
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Each subtype search is restricted — reveals its pick and may fail to find")
    void searchesAreRestricted() {
        setupAndCast();
        setupLibrary(new Plains(), new Island(), new Swamp(), new Mountain());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("A basic land type absent from the library is skipped; the others are still found")
    void absentSubtypeIsSkipped() {
        setupAndCast();
        setupLibrary(new Plains(), new Swamp(), new Mountain()); // no Island

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertNextSearchIsFor(gd, "Plains");
        pickFirstMatch(gd);
        // The Island search auto-resolves as "no match" and resolution continues straight to Swamp.
        assertNextSearchIsFor(gd, "Swamp");
        pickFirstMatch(gd);
        assertNextSearchIsFor(gd, "Mountain");
        pickFirstMatch(gd);

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
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shard Convergence"));
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new ShardConvergence()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {3}{G}
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }

    private void assertNextSearchIsFor(GameData gd, String subtypeName) {
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).as("expected a library search for %s", subtypeName).isNotNull();
        assertThat(search.params().cards().stream().map(Card::getName))
                .as("search should only present %s cards", subtypeName)
                .containsExactly(subtypeName);
    }

    private void pickFirstMatch(GameData gd) {
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }
}
