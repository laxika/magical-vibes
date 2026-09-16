package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TradeSecrets.class, Forest.class})
class TradeSecretsTest extends BaseCardTest {

    private void castTradeSecrets() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TradeSecrets()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Target opponent draws two and controller chooses up to four cards")
    void targetOpponentDrawsAndControllerDrawsUpToFour() {
        int initialOpponentHandSize = gd.playerHands.get(player2.getId()).size();
        castTradeSecrets();

        PendingInteraction.XValueChoice drawChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(drawChoice).isNotNull();
        assertThat(drawChoice.playerId()).isEqualTo(player1.getId());
        assertThat(drawChoice.maxValue()).isEqualTo(4);

        harness.handleXValueChosen(player1, 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(initialOpponentHandSize + 2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Target opponent controls repeating the entire process")
    void opponentMayRepeatTheEntireProcess() {
        int initialOpponentHandSize = gd.playerHands.get(player2.getId()).size();
        castTradeSecrets();

        harness.handleXValueChosen(player1, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 1);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(initialOpponentHandSize + 4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Controller may draw zero cards before the opponent declines to repeat")
    void controllerMayDrawZeroCards() {
        int initialOpponentHandSize = gd.playerHands.get(player2.getId()).size();
        castTradeSecrets();

        harness.handleXValueChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(initialOpponentHandSize + 2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Target opponent may repeat the entire process multiple times")
    void opponentMayRepeatMultipleTimes() {
        int initialOpponentHandSize = gd.playerHands.get(player2.getId()).size();
        castTradeSecrets();

        harness.handleXValueChosen(player1, 1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleXValueChosen(player1, 1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleXValueChosen(player1, 1);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(initialOpponentHandSize + 6);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Trade Secrets cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new TradeSecrets()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
