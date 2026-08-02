package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GracefulAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Controller has no maximum hand size — no discard during cleanup")
    void noMaximumHandSizeForController() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new GracefulAdept());

        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Mountain(),
                new Plains(), new Plains(), new Plains()
        )));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Opponent's Graceful Adept does not remove your hand limit")
    void opponentAdeptDoesNotHelp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player2, new GracefulAdept());

        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Mountain(),
                new Plains(), new Plains(), new Plains()
        )));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }
}
