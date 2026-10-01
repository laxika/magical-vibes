package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InescapableBrute;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gloomlance.class, DevotedDruid.class, SafeholdSentry.class, InescapableBrute.class,
        Forest.class, Swamp.class})
class GloomlanceTest extends BaseCardTest {

    // ===== Green creature: destroyed + controller discards =====

    @Test
    @DisplayName("Green creature is destroyed and its controller discards a card")
    void greenCreatureDestroyedAndDiscards() {
        UUID target = addCreatureReady(player2, new DevotedDruid()).getId();
        harness.setHand(player2, List.of(new Swamp(), new Forest()));
        castGloomlance(target);

        // Discard runs first while the creature is still on the battlefield.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0); // player2 discards Swamp

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Swamp");
        // ...and the creature is destroyed after the discard resolves.
        harness.assertInGraveyard(player2, "Devoted Druid");
    }

    // ===== White creature: destroyed + controller discards =====

    @Test
    @DisplayName("White creature is destroyed and its controller discards a card")
    void whiteCreatureDestroyedAndDiscards() {
        UUID target = addCreatureReady(player2, new SafeholdSentry()).getId();
        harness.setHand(player2, List.of(new Swamp()));
        castGloomlance(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Safehold Sentry");
    }

    // ===== Non-green-non-white creature: destroyed, no discard =====

    @Test
    @DisplayName("Red creature is destroyed but its controller does not discard")
    void redCreatureDestroyedNoDiscard() {
        UUID target = addCreatureReady(player2, new InescapableBrute()).getId();
        harness.setHand(player2, List.of(new Swamp(), new Forest()));
        castGloomlance(target);

        // No discard prompt — the creature was neither green nor white.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Inescapable Brute");
    }

    // ===== Green creature but empty hand: destroyed, nothing to discard =====

    @Test
    @DisplayName("Green creature with empty-handed controller is still destroyed")
    void greenCreatureEmptyHandStillDestroyed() {
        UUID target = addCreatureReady(player2, new DevotedDruid()).getId();
        harness.setHand(player2, List.of());
        castGloomlance(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Devoted Druid");
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        UUID land = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Gloomlance()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castGloomlance(UUID targetId) {
        harness.setHand(player1, List.of(new Gloomlance()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();
    }
}
