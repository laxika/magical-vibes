package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FeralMaaka;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SaruliCaretaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RumblingRuin.class, GrizzlyBears.class, LlanowarElves.class,
        FeralMaaka.class, SaruliCaretaker.class})
class RumblingRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Opposing creatures with power at most the controlled +1/+1 counter count can't block")
    void opposingCreaturesAtOrBelowCounterCountCantBlock() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ownCreatureWithCounters = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        ownCreatureWithCounters.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent opposingSmallCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingLargeCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingLargeCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castRumblingRuin();

        assertThat(bls.canBlockAttacker(gd, ownCreature, opposingSmallCreature,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, ownCreatureWithCounters, opposingSmallCreature,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, opposingSmallCreature, ownCreature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, opposingLargeCreature, ownCreature,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("No controlled +1/+1 counters leave positive-power opposing creatures able to block")
    void noCountersDoNotAffectPositivePowerCreatures() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castRumblingRuin();

        assertCanBlock(opposingCreature, true);
    }

    @Test
    void addingCountersAfterResolutionDoesNotRaiseThreshold() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FeralMaaka());

        castRumblingRuin();
        assertCanBlock(blocker, true);
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertCanBlock(blocker, true);
    }

    @Test
    void removingCountersAfterResolutionDoesNotLowerThreshold() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FeralMaaka());

        castRumblingRuin();
        assertCanBlock(blocker, false);
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertCanBlock(blocker, false);
    }

    @Test
    void counterTotalIsCountedWhenTriggerResolves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FeralMaaka());
        harness.castFromHand(player1, new RumblingRuin(), "{5}{R}");
        harness.passBothPriorities();
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        assertCanBlock(blocker, false);
    }

    @Test
    void blockingRestrictionTracksOpposingPowerChanges() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FeralMaaka());

        castRumblingRuin();
        assertCanBlock(blocker, false);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertCanBlock(blocker, true);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertCanBlock(blocker, false);
    }

    @Test
    void creaturesEnteringLaterAreAlsoRestricted() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FeralMaaka());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castRumblingRuin();

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FeralMaaka());

        assertCanBlock(blocker, false);
    }

    @Test
    void zeroCountersStillPreventZeroPowerCreaturesFromBlocking() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SaruliCaretaker());

        castRumblingRuin();

        assertCanBlock(blocker, false);
    }

    private void assertCanBlock(Permanent blocker, boolean expected) {
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RumblingRuin)
                .findFirst().orElseThrow();
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isEqualTo(expected);
    }

    private void castRumblingRuin() {
        harness.castFromHand(player1, new RumblingRuin(), "{5}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
