package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostLitStalker.class, GhostLitRedeemer.class, GhostLitWarder.class})
class GhostLitStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability makes the target player discard two cards")
    void battlefieldAbilityDiscardsTwoCards() {
        Permanent stalker = addCreatureReady(player1, new GhostLitStalker());
        harness.setHand(player2, List.of(new GhostLitRedeemer(), new GhostLitWarder(), new GhostLitRedeemer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(stalker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Channel makes the target player discard four cards and discards Ghost-Lit Stalker")
    void channelDiscardsFourCards() {
        harness.setHand(player1, List.of(new GhostLitStalker()));
        harness.setHand(player2, List.of(new GhostLitRedeemer(), new GhostLitWarder(), new GhostLitRedeemer(),
                new GhostLitWarder(), new GhostLitRedeemer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateHandAbility(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(4);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Ghost-Lit Stalker");
    }

    @Test
    @DisplayName("Cannot activate the discard ability with a permanent as its target")
    void rejectsPermanentTarget() {
        addCreatureReady(player1, new GhostLitStalker());
        harness.addToBattlefield(player2, new GhostLitWarder());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        java.util.UUID permanentId = harness.getPermanentId(player2, "Ghost-Lit Warder");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, permanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("Channel cannot target a permanent")
    void channelRejectsPermanentTarget() {
        harness.setHand(player1, List.of(new GhostLitStalker()));
        harness.addToBattlefield(player2, new GhostLitWarder());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        java.util.UUID permanentId = harness.getPermanentId(player2, "Ghost-Lit Warder");

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }
}
