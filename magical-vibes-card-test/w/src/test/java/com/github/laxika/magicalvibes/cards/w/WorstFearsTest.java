package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WorstFears.class})
class WorstFearsTest extends BaseCardTest {

    @Test
    @DisplayName("Controls the target player's next turn and exiles itself")
    void controlsTargetPlayersNextTurnAndExilesItself() {
        harness.setHand(player1, List.of(new WorstFears()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Worst Fears");
        harness.assertNotInGraveyard(player1, "Worst Fears");
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        harness.setHand(player1, List.of(new WorstFears()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(player1.getId(), player1.getId());
    }

    @Test
    @DisplayName("Control starts on the target's next turn and ends before the following turn")
    void controlLastsForOnlyTheNextTurn() {
        harness.setHand(player1, List.of(new WorstFears()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.mindControlledPlayerId).isNull();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.pendingTurnControl).isEmpty();

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.mindControlledPlayerId).isNull();
        assertThat(gd.mindControllerPlayerId).isNull();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.mindControlledPlayerId).isNull();
    }

    @Test
    @DisplayName("Controller casts using the controlled player's hand and mana")
    void controllerUsesControlledPlayersResources() {
        harness.setHand(player1, List.of(new WorstFears()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WorstFears()));
        harness.addMana(player2, ManaColor.BLACK, 8);

        gs.playCard(gd, player1, 0, 0, player1.getId(), null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertNotInHand(player2, "Worst Fears");
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(player1.getId(), player2.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).contains("Worst Fears");
    }

    @Test
    @DisplayName("Controlled player cannot independently choose to cast a spell")
    void controlledPlayerCannotCastIndependently() {
        harness.setHand(player1, List.of(new WorstFears()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WorstFears()));
        harness.addMana(player2, ManaColor.BLACK, 8);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player2, "Worst Fears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(8);
    }
}
