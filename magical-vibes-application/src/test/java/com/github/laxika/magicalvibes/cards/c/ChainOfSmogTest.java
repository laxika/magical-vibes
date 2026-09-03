package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainOfSmog.class, GrizzlyBears.class})
class ChainOfSmogTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards")
    void targetPlayerDiscardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The target player may copy the spell")
    void targetPlayerMayCopySpell() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        castAtPlayer2();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining the copy ends the spell")
    void decliningCopyEndsSpell() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        castAtPlayer2();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Rejects a permanent as the target")
    void rejectsPermanentTarget() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChainOfSmog()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAtPlayer2() {
        harness.setHand(player1, List.of(new ChainOfSmog()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
