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
