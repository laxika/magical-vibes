package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GracefulAdept.class, Forest.class, Mountain.class, Plains.class, Humble.class})
class GracefulAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Controller has no maximum hand size — no discard during cleanup")
    void noMaximumHandSizeForController() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new GracefulAdept());

        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Mountain(),
                new Plains(), new Plains(), new Plains()
        ));

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

        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Mountain(),
                new Plains(), new Plains(), new Plains()
        ));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing all abilities restores the hand limit before until-end-of-turn effects expire")
    void losingAbilitiesRestoresHandLimit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        var adept = harness.addToBattlefieldAndReturn(player1, new GracefulAdept());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, adept.getId());

        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Mountain(),
                new Plains(), new Plains(), new Plains()
        ));
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }
}
