package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalduvianHordeTest extends BaseCardTest {

    // ===== ETB prompt =====

    @Test
    @DisplayName("ETB with a card in hand prompts the may ability choice")
    void etbWithCardInHandPromptsMayAbility() {
        castBalduvianHordeWithCardInHand();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    // ===== Accept — random discard, no card choice =====

    @Test
    @DisplayName("Accepting discards a card at random and keeps Balduvian Horde")
    void acceptingDiscardsAtRandomAndKeepsHorde() {
        castBalduvianHordeWithCardInHand();

        harness.handleMayAbilityChosen(player1, true);

        // No discard choice — the discard is at random
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Balduvian Horde stays on the battlefield
        harness.assertOnBattlefield(player1, "Balduvian Horde");

        // The lone card was discarded at random
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Decline — sacrifice =====

    @Test
    @DisplayName("Declining sacrifices Balduvian Horde and leaves the hand untouched")
    void decliningSacrificesHorde() {
        castBalduvianHordeWithCardInHand();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Balduvian Horde");
        harness.assertInGraveyard(player1, "Balduvian Horde");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    // ===== Empty hand — auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices with no card to discard")
    void autoSacrificesWithEmptyHand() {
        harness.setHand(player1, List.of(new BalduvianHorde()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of());
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Balduvian Horde");
        harness.assertInGraveyard(player1, "Balduvian Horde");

        // No prompt — it was automatic
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Helpers =====

    /**
     * Casts Balduvian Horde with a single card (Grizzly Bears) in hand so the random
     * discard is deterministic, resolving through to the may ability prompt.
     */
    private void castBalduvianHordeWithCardInHand() {
        harness.setHand(player1, List.of(new BalduvianHorde()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
