package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.ForgeDevil;
import com.github.laxika.magicalvibes.cards.h.HeadlessSkaab;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkovBlademaster.class, HeadlessSkaab.class, ForgeDevil.class})
class MarkovBlademasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets two +1/+1 counters and deals 3 damage from double strike")
    void doubleStrikeIsRulesCorrect() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new MarkovBlademaster());
        blademaster.setSummoningSick(false);
        blademaster.setAttacking(true);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Gets no counter when blocked and dealing no combat damage to a player")
    void blockedDealsNoCounter() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new MarkovBlademaster());
        blademaster.setSummoningSick(false);
        blademaster.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Each Blademaster gets its own counters, one per damage event regardless of damage amount")
    void countersBelongToEachDamageSource() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MarkovBlademaster());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MarkovBlademaster());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        first.setSummoningSick(false);
        second.setSummoningSick(false);
        first.setAttacking(true);
        second.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("A Blademaster that did not deal combat damage gets no counters")
    void nonAttackingBlademasterGetsNoCounters() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MarkovBlademaster());
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new MarkovBlademaster());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bystander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Killing the blocker with first-strike damage does not allow regular damage to the player")
    void remainsBlockedAfterKillingBlocker() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new MarkovBlademaster());
        blademaster.setSummoningSick(false);
        blademaster.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ForgeDevil());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Forge Devil");
        harness.assertOnBattlefield(player1, "Markov Blademaster");
        assertThat(blademaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
