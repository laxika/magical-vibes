package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinBoarders.class})
class GoblinBoardersTest extends BaseCardTest {

    @Test
    void entersWithoutRaidWithoutCounter() {
        castBoarders(false);

        assertThat(findBoarders().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithRaidWithCounter() {
        castBoarders(true);

        assertThat(findBoarders().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        castBoarders(false);

        assertThat(findBoarders().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterIsPresentOnEntryWithoutATrigger() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        Permanent boarders = harness.enterBattlefieldAndReturn(player1, new GoblinBoarders());

        assertThat(boarders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutCastingStillChecksItsControllerForRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        Permanent boarders = harness.enterBattlefieldAndReturn(player2, new GoblinBoarders());

        assertThat(boarders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void raidStillAppliesAfterTheAttackerLeavesTheBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GoblinBoarders());
        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        castBoarders(false);

        assertThat(findBoarders().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void declaringNoAttackersDoesNotEnableRaid() {
        addCreatureReady(player1, new GoblinBoarders());
        declareAttackers(List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        castBoarders(false);

        assertThat(findPermanents(player1, "Goblin Boarders"))
                .allSatisfy(boarders -> assertThat(boarders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    private void castBoarders(boolean raid) {
        if (raid) {
            gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        }
        harness.castFromHand(player1, new GoblinBoarders(), "{2}{R}");
        harness.passBothPriorities();
    }

    private Permanent findBoarders() {
        return findPermanent(player1, "Goblin Boarders");
    }
}
