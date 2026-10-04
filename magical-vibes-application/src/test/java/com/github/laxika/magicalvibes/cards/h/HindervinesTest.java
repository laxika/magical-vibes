package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hindervines.class, GrizzlyBears.class})
class HindervinesTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with no +1/+1 counters are prevented from dealing combat damage")
    void preventsCreaturesWithoutCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHindervines();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();
        harness.assertInGraveyard(player1, "Hindervines");
    }

    @Test
    @DisplayName("Creatures with a +1/+1 counter still deal combat damage")
    void exemptsCreaturesWithCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHindervines();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    @Test
    @DisplayName("Counters gained after resolution exempt the creature")
    void countersAddedLaterExempt() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHindervines();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    @Test
    @DisplayName("Losing the last +1/+1 counter after resolution enables prevention")
    void countersRemovedLaterEnablePrevention() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHindervines();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution are covered, including opposing creatures")
    void creaturesEnteringLaterAreCovered() {
        castHindervines();

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    @Test
    @DisplayName("Other counter types do not exempt a creature")
    void otherCountersDoNotExempt() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        castHindervines();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
    }

    @Test
    @DisplayName("Only an unblocked attacker with +1/+1 counters deals combat damage")
    void onlyCounterBearingAttackerDamagesPlayer() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent counterBearer = addCreatureReady(player1, new GrizzlyBears());
        counterBearer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        castHindervines();
        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A counter-bearing blocker deals damage while its attacker deals none")
    void onlyCounterBearingBlockerDealsDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHindervines();
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHindervines();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    private void castHindervines() {
        harness.castFromHand(player1, new Hindervines(), "{2}{G}");
        harness.passBothPriorities();
    }
}
