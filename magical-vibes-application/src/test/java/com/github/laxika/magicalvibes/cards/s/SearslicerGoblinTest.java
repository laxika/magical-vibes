package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearslicerGoblinTest extends BaseCardTest {

    @Test
    void createsGoblinTokenAtEndStepAfterAttacking() {
        harness.addToBattlefield(player1, new SearslicerGoblin());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep(player1);

        assertThat(goblinTokenCount()).isEqualTo(1);
    }

    @Test
    void doesNotCreateGoblinTokenWithoutAttacking() {
        harness.addToBattlefield(player1, new SearslicerGoblin());

        advanceToEndStep(player1);

        assertThat(goblinTokenCount()).isZero();
    }

    @Test
    void opponentAttackDoesNotEnableRaid() {
        harness.addToBattlefield(player1, new SearslicerGoblin());
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        advanceToEndStep(player1);

        assertThat(goblinTokenCount()).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long goblinTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Goblin"))
                .count();
    }
}
