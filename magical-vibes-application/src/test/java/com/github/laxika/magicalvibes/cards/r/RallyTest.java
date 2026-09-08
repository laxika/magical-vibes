package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rally.class, BalduvianBears.class})
class RallyTest extends BaseCardTest {

    @Test
    @DisplayName("Rally boosts only blocking creatures with +1/+1")
    void boostsBlockingCreatures() {
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        blocker.setBlocking(true);
        Permanent nonBlocker = addCreatureReady(player2, new BalduvianBears());
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        castRally();

        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);

        assertThat(nonBlocker.getEffectivePower()).isEqualTo(2);
        assertThat(nonBlocker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rally boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        blocker.setBlocking(true);

        castRally();

        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rally affects blockers present when it resolves, not creatures that block later")
    void boostIsLockedInAtResolution() {
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        blocker.setBlocking(true);
        Permanent laterBlocker = addCreatureReady(player2, new BalduvianBears());

        castRally();

        blocker.setBlocking(false);
        laterBlocker.setBlocking(true);

        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
        assertThat(laterBlocker.getEffectivePower()).isEqualTo(2);
        assertThat(laterBlocker.getEffectiveToughness()).isEqualTo(2);
    }

    private void castRally() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castFromHand(player1, new Rally(), "{W}{W}");
        harness.passBothPriorities();
    }
}
