package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtNibbler.class, Forest.class, Mountain.class, Plains.class})
class ThoughtNibblerTest extends BaseCardTest {

    @Test
    @DisplayName("Controller must discard down to five during cleanup")
    void controllerMaximumHandSizeIsReducedByTwo() {
        harness.addToBattlefield(player1, new ThoughtNibbler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Mountain(), new Plains(),
                new Forest(), new Forest()
        ));

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Thought Nibbler does not reduce an opponent's maximum hand size")
    void opponentMaximumHandSizeIsUnaffected() {
        harness.addToBattlefield(player1, new ThoughtNibbler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Mountain(), new Plains(), new Plains()
        ));

        harness.passUntil(player2, TurnStep.CLEANUP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Controller's hand size returns to seven when Thought Nibbler leaves")
    void reductionEndsWhenSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new ThoughtNibbler());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Mountain(), new Plains(), new Plains()
        ));

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Controller does not discard when hand is at the reduced maximum")
    void controllerDoesNotDiscardAtReducedMaximum() {
        harness.addToBattlefield(player1, new ThoughtNibbler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Mountain(), new Plains(), new Forest()
        ));

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }
}
