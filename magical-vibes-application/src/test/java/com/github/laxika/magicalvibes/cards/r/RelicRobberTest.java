package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelicRobber.class})
class RelicRobberTest extends BaseCardTest {

    @Test
    void damagedPlayerCreatesTheConstruct() {
        Permanent robber = addCreatureReady(player1, new RelicRobber());
        robber.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Construct")).isEmpty();
        assertThat(findPermanents(player2, "Goblin Construct")).hasSize(1);
    }

    @Test
    void constructCannotBlock() {
        Permanent construct = createConstructForPlayer2();

        assertThat(bls.canBlock(gd, construct)).isFalse();
    }

    @Test
    void constructDamagesItsControllerAtUpkeep() {
        createConstructForPlayer2();
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent createConstructForPlayer2() {
        Permanent robber = addCreatureReady(player1, new RelicRobber());
        robber.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanents(player2, "Goblin Construct").getFirst();
    }
}
