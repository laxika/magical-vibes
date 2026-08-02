package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BiovisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game at the end step with four Biovisionaries")
    void winsWithFourCopies() {
        addCopies(player1, 4);

        advanceToEndStepAndResolve(player1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger with only three Biovisionaries")
    void noTriggerWithThreeCopies() {
        addCopies(player1, 3);

        advanceToEndStepAndResolve(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Other creatures do not count toward the four")
    void otherCreaturesDoNotCount() {
        addCopies(player1, 3);
        addPermanent(player1, new GrizzlyBears());

        advanceToEndStepAndResolve(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Copies controlled by an opponent do not count")
    void opponentCopiesDoNotCount() {
        addCopies(player1, 2);
        addCopies(player2, 2);

        advanceToEndStepAndResolve(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Triggers at an opponent's end step too")
    void triggersOnOpponentEndStep() {
        addCopies(player1, 4);

        advanceToEndStepAndResolve(player2);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCopies(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addPermanent(player, new Biovisionary());
        }
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
