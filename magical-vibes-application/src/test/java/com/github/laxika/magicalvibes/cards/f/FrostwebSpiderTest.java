package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FrostwebSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature with flying puts a +1/+1 counter on Frostweb Spider at end of combat")
    void blockingFlyingCreaturePutsCounterAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new Ornithopter());
        attacker.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new FrostwebSpider());

        declareBlock(spider, attacker);
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        leaveEndOfCombat();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking a creature without flying does not put a +1/+1 counter on Frostweb Spider")
    void blockingNonFlyingCreatureDoesNotPutCounter() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new FrostwebSpider());

        declareBlock(spider, attacker);
        harness.passBothPriorities();
        leaveEndOfCombat();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
