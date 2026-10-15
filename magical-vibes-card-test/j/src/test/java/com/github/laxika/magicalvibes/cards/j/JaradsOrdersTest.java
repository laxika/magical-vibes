package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({JaradsOrders.class, DrudgeBeetle.class, Plains.class, Swamp.class})
class JaradsOrdersTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only creature cards, revealed, fail-to-find allowed")
    void resolvingPresentsCreaturesOnly() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Drudge Beetle"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().followUp().cardToGraveyard()).isNotNull();
    }

    @Test
    @DisplayName("Two creatures: one to hand, one to graveyard")
    void bothPicks() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Drudge Beetle");

        var second = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(second.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(second.params().reveals()).isTrue();
        assertThat(second.params().canFailToFind()).isTrue();
        assertThat(second.params().cards()).hasSize(1);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Drudge Beetle");
        harness.assertInGraveyard(player1, "Jarad's Orders");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding only one creature puts it into hand (ruling)")
    void findOnlyOne() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);
        // Decline the graveyard pick
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Drudge Beetle");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Drudge Beetle"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding zero creatures shuffles and finishes")
    void findZero() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Jarad's Orders");
    }

    @Test
    @DisplayName("No creatures in library shuffles without a prompt")
    void noCreatures() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Jarad's Orders");
    }

    @Test
    @DisplayName("The only creature in the library goes to hand without a second prompt")
    void onlyCreatureGoesToHand() {
        setupAndCast();
        DrudgeBeetle creature = new DrudgeBeetle();
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land, creature));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Jarad's Orders");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("An empty library finishes resolution without a choice")
    void emptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Jarad's Orders");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new JaradsOrders()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new DrudgeBeetle(), new DrudgeBeetle()));
    }
}
