package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostLitStalker.class, GhostLitRedeemer.class, GhostLitWarder.class})
class GhostLitStalkerTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void canTargetSelf(boolean channel) {
        prepareAbility(channel);
        if (channel) {
            harness.setHand(player1, List.of(new GhostLitStalker(), new GhostLitRedeemer(),
                    new GhostLitWarder(), new GhostLitRedeemer(), new GhostLitWarder(), new GhostLitRedeemer()));
        } else {
            harness.setHand(player1, List.of(new GhostLitRedeemer(), new GhostLitWarder(), new GhostLitRedeemer()));
        }

        activate(channel, player1.getId());
        if (channel) {
            harness.assertInGraveyard(player1, "Ghost-Lit Stalker");
            assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        }
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        for (int i = 0; i < (channel ? 4 : 2); i++) {
            harness.handleCardChosen(player1, 0);
        }
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(channel ? 5 : 2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void discardsAllCardsFromShortHand(boolean channel) {
        prepareAbility(channel);
        harness.setHand(player2, List.of(new GhostLitRedeemer()));

        activate(channel, player2.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Ghost-Lit Redeemer");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void resolvesAgainstEmptyHand(boolean channel) {
        prepareAbility(channel);
        harness.setHand(player2, List.of());

        activate(channel, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cannotActivateOutsideMainPhase(boolean channel) {
        prepareAbility(channel);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> activate(channel, player2.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cannotActivateDuringOpponentsTurn(boolean channel) {
        prepareAbility(channel);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> activate(channel, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cannotActivateWithNonemptyStack(boolean channel) {
        prepareAbility(channel);
        addCreatureReady(player1, new GhostLitRedeemer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, channel ? 0 : 1, null, null);

        assertThatThrownBy(() -> activate(channel, player2.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("stack");
    }

    private void prepareAbility(boolean channel) {
        if (channel) {
            harness.setHand(player1, List.of(new GhostLitStalker()));
        } else {
            addCreatureReady(player1, new GhostLitStalker());
        }
        harness.addMana(player1, ManaColor.BLACK, channel ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, channel ? 5 : 4);
    }

    private void activate(boolean channel, java.util.UUID targetId) {
        if (channel) {
            harness.activateHandAbility(player1, 0, targetId);
        } else {
            harness.activateAbility(player1, 0, null, targetId);
        }
    }

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
