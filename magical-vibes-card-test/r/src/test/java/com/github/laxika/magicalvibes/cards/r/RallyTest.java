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

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);

        assertThat(gqs.getEffectivePower(gd, nonBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rally boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        blocker.setBlocking(true);

        castRally();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
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

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBlocker)).isEqualTo(2);
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

        assertThat(gqs.getEffectivePower(gd, firstBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonBlocker)).isEqualTo(2);
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
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
