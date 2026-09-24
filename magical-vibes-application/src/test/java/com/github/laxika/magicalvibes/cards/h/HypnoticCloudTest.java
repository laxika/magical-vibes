package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HypnoticCloud.class, Forest.class})
class HypnoticCloudTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, target player discards one card")
    void discardsOneWithoutKicker() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));
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
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest(), new Forest())));
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
    @DisplayName("Can target the casting player")
    void canTargetController() {
        harness.setHand(player1, List.of(new HypnoticCloud(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Rejects a permanent as the target")
    void rejectsPermanentTarget() {
        harness.addToBattlefield(player2, new Forest());
        UUID permanentId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new HypnoticCloud()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }
}
