package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rebirth.class, GrizzlyBears.class, ScatheZombies.class, DurkwoodBoars.class, Riftsweeper.class})
class RebirthTest extends BaseCardTest {

    private void castRebirth() {
        castRebirth(player1);
    }

    private void castRebirth(Player caster) {
        harness.castFromHand(caster, new Rebirth(), "{3}{G}{G}{G}");
        harness.passBothPriorities(); // resolve Rebirth -> active player prompted first (APNAP)
    }

    @Test
    @DisplayName("Each anteing player antes their top card and their life becomes 20 (up or down)")
    void bothPlayersAnte() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 6);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new DurkwoodBoars()));
        harness.setLibrary(player2, List.of(new ScatheZombies()));

        castRebirth();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        // Life set to 20 in both directions (30 -> 20 down, 6 -> 20 up).
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        // The anted top card leaves the library for the ante zone.
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.antedCardIds).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Durkwood Boars");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves that player's library and life untouched")
    void decliningChangesNothing() {
        harness.setLife(player1, 25);
        harness.setLife(player2, 15);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new ScatheZombies()));

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
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.antedCardIds).hasSize(1);
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
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.antedCardIds).hasSize(1);

        // Player2 could not ante: life unchanged, nothing anted.
        harness.assertLife(player2, 12);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("May choices are offered in active-player-first order")
    void promptsActivePlayerFirst() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 6);
        harness.setLife(player2, 30);
        GrizzlyBears player1Top = new GrizzlyBears();
        DurkwoodBoars player2Top = new DurkwoodBoars();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of(player2Top));

        castRebirth(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(((PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(((PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 30);
        assertThat(gd.antedCardIds).containsExactly(player1Top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Top);
    }

    @Test
    @DisplayName("An accepted card is recorded as anted")
    void acceptedCardIsMarkedAsAnted() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());

        castRebirth();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.antedCardIds).containsExactly(topCard.getId());
    }

    @Test
    @DisplayName("All players choose before any cards are anted or life totals change")
    void allPlayersChooseBeforeActionsOccur() {
        Card firstTop = new GrizzlyBears();
        Card secondTop = new ScatheZombies();
        harness.setLibrary(player1, List.of(firstTop));
        harness.setLibrary(player2, List.of(secondTop));
        harness.setLife(player1, 6);
        harness.setLife(player2, 30);

        castRebirth();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertLife(player1, 6);
        harness.assertLife(player2, 30);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondTop);
        assertThat(gd.antedCardIds).isEmpty();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.antedCardIds).containsExactlyInAnyOrder(firstTop.getId(), secondTop.getId());
    }

    @Test
    @CardUsed({Rebirth.class, GrizzlyBears.class, Riftsweeper.class})
    @DisplayName("Riftsweeper cannot target a card in the ante zone")
    void antedCardCannotBeTargetedAsExiled() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());
        castRebirth();
        harness.handleMayAbilityChosen(player1, true);

        harness.castFromHand(player1, new Riftsweeper(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.antedCardIds).containsExactly(topCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Both players declining preserves both libraries and life totals")
    void bothPlayersDecline() {
        Card firstTop = new GrizzlyBears();
        Card secondTop = new ScatheZombies();
        harness.setLibrary(player1, List.of(firstTop));
        harness.setLibrary(player2, List.of(secondTop));
        harness.setLife(player1, 6);
        harness.setLife(player2, 30);

        castRebirth();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, 6);
        harness.assertLife(player2, 30);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondTop);
        assertThat(gd.antedCardIds).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
