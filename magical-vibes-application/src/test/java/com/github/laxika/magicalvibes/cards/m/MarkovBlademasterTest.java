package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkovBlademasterTest extends BaseCardTest {

    

    @Test
    @DisplayName("Gets two +1/+1 counters and deals 3 damage from double strike")
    void doubleStrikeIsRulesCorrect() {
        harness.addToBattlefield(player1, new MarkovBlademaster());

        Permanent blademaster = gd.playerBattlefields.get(player1.getId()).getFirst();
        blademaster.setSummoningSick(false);
        blademaster.setAttacking(true);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Gets no counter when blocked and dealing no combat damage to a player")
    void blockedDealsNoCounter() {
        harness.addToBattlefield(player1, new MarkovBlademaster());

        Permanent blademaster = gd.playerBattlefields.get(player1.getId()).getFirst();
        blademaster.setSummoningSick(false);
        blademaster.setAttacking(true);

        GrizzlyBears wall = new GrizzlyBears();
        wall.setToughness(6);
        harness.addToBattlefield(player2, wall);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }
}
