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

    @Test
    @DisplayName("The defending player can boost all of their blockers with Rally")
    void defendingPlayerBoostsMultipleBlockers() {
        Permanent firstBlocker = addCreatureReady(player2, new BalduvianBears());
        firstBlocker.setBlocking(true);
        Permanent secondBlocker = addCreatureReady(player2, new BalduvianBears());
        secondBlocker.setBlocking(true);
        Permanent nonBlocker = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castFromHand(player2, new Rally(), "{W}{W}");
        harness.passBothPriorities();

        assertThat(firstBlocker.getEffectivePower()).isEqualTo(3);
        assertThat(firstBlocker.getEffectiveToughness()).isEqualTo(3);
        assertThat(secondBlocker.getEffectivePower()).isEqualTo(3);
        assertThat(secondBlocker.getEffectiveToughness()).isEqualTo(3);
        assertThat(nonBlocker.getEffectivePower()).isEqualTo(2);
        assertThat(nonBlocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rally resolves without blockers and does not boost creatures that block afterward")
    void resolvesWithoutBlockers() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Rally(), "{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rally");
        creature.setBlocking(true);
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }
}
