package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class HypnoticCloudTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, target player discards one card")
    void discardsOneWithoutKicker() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Peek(), new Peek())));
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With kicker, target player discards three cards instead")
    void discardsThreeWithKicker() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Peek(), new Peek(), new Peek())));
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(),
                false, null, null, List.of(), null, List.of(), true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Rejects a permanent as the target")
    void rejectsPermanentTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID permanentId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }
}
