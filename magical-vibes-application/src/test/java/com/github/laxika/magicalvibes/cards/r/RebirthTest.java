package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RebirthTest extends BaseCardTest {

    private void castRebirth() {
        harness.setHand(player1, List.of(new Rebirth()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Rebirth -> active player prompted first (APNAP)
    }

    @Test
    @DisplayName("Each anteing player exiles their top card and their life becomes 20 (up or down)")
    void bothPlayersAnte() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 6);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setLibrary(player2, List.of(new AirElemental()));

        castRebirth();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        // Life set to 20 in both directions (30 -> 20 down, 6 -> 20 up).
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        // The anted top card leaves the library for exile.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactly("Air Elemental");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves that player's library and life untouched")
    void decliningChangesNothing() {
        harness.setLife(player1, 25);
        harness.setLife(player2, 15);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new AirElemental()));

        castRebirth();

        harness.handleMayAbilityChosen(player1, false); // player1 declines
        harness.handleMayAbilityChosen(player2, true);  // player2 antes

        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        // Player 1 declined: nothing anted, life unchanged.
        harness.assertLife(player1, 25);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Grizzly Bears");

        // Player 2 anted.
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactly("Air Elemental");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player with an empty library is never prompted and cannot ante")
    void emptyLibraryNotPrompted() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of());

        castRebirth();

        // Only player1 (non-empty library) is offered the ante decision.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(((PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // No prompt was ever queued for player2, so resolution finishes.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Grizzly Bears");

        // Player2 could not ante: life unchanged, nothing exiled.
        harness.assertLife(player2, 12);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
