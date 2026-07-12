package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GloomlanceTest extends BaseCardTest {

    // ===== Green creature: destroyed + controller discards =====

    @Test
    @DisplayName("Green creature is destroyed and its controller discards a card")
    void greenCreatureDestroyedAndDiscards() {
        UUID target = addCreature(player2, new GrizzlyBears()); // green
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest())));
        castGloomlance(target);

        // Discard runs first while the creature is still on the battlefield.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0); // player2 discards Peek

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        // ...and the creature is destroyed after the discard resolves.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== White creature: destroyed + controller discards =====

    @Test
    @DisplayName("White creature is destroyed and its controller discards a card")
    void whiteCreatureDestroyedAndDiscards() {
        UUID target = addCreature(player2, new EliteVanguard()); // white
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        castGloomlance(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
    }

    // ===== Non-green-non-white creature: destroyed, no discard =====

    @Test
    @DisplayName("Red creature is destroyed but its controller does not discard")
    void redCreatureDestroyedNoDiscard() {
        UUID target = addCreature(player2, new HillGiant()); // red
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest())));
        castGloomlance(target);

        // No discard prompt — the creature was neither green nor white.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    // ===== Green creature but empty hand: destroyed, nothing to discard =====

    @Test
    @DisplayName("Green creature with empty-handed controller is still destroyed")
    void greenCreatureEmptyHandStillDestroyed() {
        UUID target = addCreature(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of()));
        castGloomlance(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID land = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new Gloomlance()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private UUID addCreature(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(owner, card);
        return harness.getPermanentId(owner, card.getName());
    }

    private void castGloomlance(UUID targetId) {
        harness.setHand(player1, List.of(new Gloomlance()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();
    }
}
