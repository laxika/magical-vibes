package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pillaging Horde"));

        // The lone card was discarded at random
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Decline — sacrifice =====

    @Test
    @DisplayName("Declining sacrifices Pillaging Horde and leaves the hand untouched")
    void decliningSacrificesHorde() {
        castPillagingHordeWithCardInHand();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pillaging Horde"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pillaging Horde"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Empty hand — auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices with no card to discard")
    void autoSacrificesWithEmptyHand() {
        harness.setHand(player1, List.of(new PillagingHorde()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of());
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pillaging Horde"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pillaging Horde"));

        // No prompt — it was automatic
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Helpers =====

    /**
     * Casts Pillaging Horde with a single card (Grizzly Bears) in hand so the random
     * discard is deterministic, resolving through to the may ability prompt.
     */
    private void castPillagingHordeWithCardInHand() {
        harness.setHand(player1, List.of(new PillagingHorde()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
