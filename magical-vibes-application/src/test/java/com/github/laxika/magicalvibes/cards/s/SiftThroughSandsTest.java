package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.p.PeerThroughDepths;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.cards.t.TheUnspeakable;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SiftThroughSands.class, PeerThroughDepths.class, ReachThroughMists.class, TheUnspeakable.class,
        LanternKami.class})
class SiftThroughSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discards a card")
    void drawsTwoThenDiscardsOne() {
        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), new LanternKami()));

        castSift(); // castSift sets the hand to the spell alone, so the hand is empty on resolution

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Sift Through Sands");
    }

    @Test
    @DisplayName("Without both named spells cast this turn there is no search")
    void noSearchWithoutBothNamedSpells() {
        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), new LanternKami()));
        castReachThroughMists();

        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), new TheUnspeakable()));
        castSift();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "The Unspeakable");
    }

    @Test
    @DisplayName("With both named spells cast this turn the controller may search for The Unspeakable")
    void offersSearchAfterBothNamedSpells() {
        harness.setLibrary(player1, List.of());
        castPeerThroughDepths(); // empty library — resolves with no interaction

        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), new LanternKami()));
        castReachThroughMists();

        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami()));
        castSift();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search puts The Unspeakable onto the battlefield")
    void acceptingSearchPutsTheUnspeakableOntoBattlefield() {
        harness.setLibrary(player1, List.of());
        castPeerThroughDepths();

        harness.setLibrary(player1, List.of(new LanternKami()));
        castReachThroughMists();

        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), new TheUnspeakable()));
        castSift();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "The Unspeakable");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Named spells still on the stack satisfy the search condition")
    void namedSpellsNeedNotResolve() {
        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), new TheUnspeakable()));
        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.castFromHand(player1, new PeerThroughDepths(), "{1}{U}");

        castSift();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "The Unspeakable");
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's named spell does not satisfy the controller's condition")
    void opponentsNamedSpellDoesNotCount() {
        harness.setLibrary(player1, List.of());
        castPeerThroughDepths();
        harness.setLibrary(player2, List.of(new LanternKami()));
        harness.castFromHand(player2, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), new TheUnspeakable()));
        castSift();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "The Unspeakable");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The controller may fail to find even when The Unspeakable is in the library")
    void mayFailToFindTheUnspeakable() {
        harness.setLibrary(player1, List.of());
        castPeerThroughDepths();
        harness.setLibrary(player1, List.of(new LanternKami()));
        castReachThroughMists();

        TheUnspeakable unspeakable = new TheUnspeakable();
        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), unspeakable));
        castSift();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unspeakable);
        harness.assertNotOnBattlefield(player1, "The Unspeakable");
        harness.assertInGraveyard(player1, "Sift Through Sands");
    }

    @Test
    @DisplayName("Accepting a search with no matching card finishes normally")
    void searchWithNoMatchingCard() {
        harness.setLibrary(player1, List.of());
        castPeerThroughDepths();
        harness.setLibrary(player1, List.of(new LanternKami()));
        castReachThroughMists();

        LanternKami remainingCard = new LanternKami();
        harness.setLibrary(player1, List.of(new LanternKami(), new LanternKami(), remainingCard));
        castSift();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        harness.assertNotOnBattlefield(player1, "The Unspeakable");
        harness.assertInGraveyard(player1, "Sift Through Sands");
    }

    private void castSift() {
        harness.castFromHand(player1, new SiftThroughSands(), "{1}{U}{U}");
        harness.passBothPriorities();
    }

    private void castReachThroughMists() {
        harness.castFromHand(player1, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();
    }

    private void castPeerThroughDepths() {
        harness.castFromHand(player1, new PeerThroughDepths(), "{1}{U}");
        harness.passBothPriorities();
    }
}
