package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.y.YuyanArchers;
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

@CardUsed({OzaisCruelty.class, YuyanArchers.class})
class OzaisCrueltyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the target player and makes them discard two cards")
    void damagesAndMakesTargetDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new YuyanArchers(), new YuyanArchers(), new YuyanArchers())));
        castOzaisCruelty(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Still deals damage when the target has no cards to discard")
    void damagesEmptyHand() {
        harness.setHand(player2, new ArrayList<>());
        castOzaisCruelty(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can target a player but not a permanent")
    void rejectsPermanentTarget() {
        var creature = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());
        harness.setHand(player1, List.of(new OzaisCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discards the only card when the target has fewer than two cards")
    void discardsOneCardHand() {
        var card = new YuyanArchers();
        harness.setHand(player2, List.of(card));
        castOzaisCruelty(player2.getId());

        harness.assertLife(player2, 18);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target its controller, who chooses and discards two cards")
    void canTargetController() {
        var first = new YuyanArchers();
        var second = new YuyanArchers();
        harness.setHand(player1, List.of(new OzaisCruelty(), first, second));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    private void castOzaisCruelty(UUID targetId) {
        harness.setHand(player1, List.of(new OzaisCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
