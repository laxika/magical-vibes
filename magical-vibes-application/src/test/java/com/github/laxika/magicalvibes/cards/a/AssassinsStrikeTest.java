package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssassinsStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature is destroyed and its controller discards a card")
    void destroysCreatureAndControllerDiscards() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID target = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest())));
        cast(target);

        // Discard runs first, while the creature is still on the battlefield.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Peek");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Empty-handed controller discards nothing but the creature is still destroyed")
    void emptyHandStillDestroys() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID target = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player2, new ArrayList<>(List.of()));
        cast(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Your own creature can be targeted, making you discard")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        UUID target = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player1, new ArrayList<>(List.of(new AssassinsStrike(), new Peek())));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID land = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new AssassinsStrike()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new AssassinsStrike()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();
    }
}
