package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PillagingHorde.class, GrizzlyBears.class})
class PillagingHordeTest extends BaseCardTest {

    // ===== ETB prompt =====

    @Test
    @DisplayName("ETB with a card in hand prompts the may ability choice")
    void etbWithCardInHandPromptsMayAbility() {
        castPillagingHordeWithCardInHand();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    // ===== Accept — random discard, no card choice =====

    @Test
    @DisplayName("Accepting discards a card at random and keeps Pillaging Horde")
    void acceptingDiscardsAtRandomAndKeepsHorde() {
        castPillagingHordeWithCardInHand();

        harness.handleMayAbilityChosen(player1, true);

        // No discard choice — the discard is at random
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Pillaging Horde stays on the battlefield
        harness.assertOnBattlefield(player1, "Pillaging Horde");

        // The lone card was discarded at random
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting with multiple cards discards exactly one without a card choice")
    void acceptingWithMultipleCardsDiscardsExactlyOneWithoutCardChoice() {
        harness.castFromHand(player1, new PillagingHorde(), "{2}{R}{R}");
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pillaging Horde");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Decline — sacrifice =====

    @Test
    @DisplayName("Declining sacrifices Pillaging Horde and leaves the hand untouched")
    void decliningSacrificesHorde() {
        castPillagingHordeWithCardInHand();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Pillaging Horde");
        harness.assertInGraveyard(player1, "Pillaging Horde");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    // ===== Empty hand — auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices with no card to discard")
    void autoSacrificesWithEmptyHand() {
        harness.castFromHand(player1, new PillagingHorde(), "{2}{R}{R}");
        harness.setHand(player1, List.of());
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Pillaging Horde");
        harness.assertInGraveyard(player1, "Pillaging Horde");

        // No prompt — it was automatic
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Helpers =====

    /**
     * Casts Pillaging Horde with a single card (Grizzly Bears) in hand so the random
     * discard is deterministic, resolving through to the may ability prompt.
     */
    private void castPillagingHordeWithCardInHand() {
        harness.castFromHand(player1, new PillagingHorde(), "{2}{R}{R}");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
