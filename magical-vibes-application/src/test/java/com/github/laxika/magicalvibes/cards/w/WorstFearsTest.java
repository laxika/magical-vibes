package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
