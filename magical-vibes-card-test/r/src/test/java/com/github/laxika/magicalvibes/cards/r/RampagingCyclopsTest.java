package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingCyclops.class})
class RampagingCyclopsTest extends BaseCardTest {

    

    @Test
    @DisplayName("Rampaging Cyclops has full power when not blocked")
    void fullPowerWhenNotBlocked() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new RampagingCyclops());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Rampaging Cyclops has full power when blocked by one creature")
    void fullPowerWhenBlockedByOne() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new RampagingCyclops());
        cyclops.setSummoningSick(false);
        cyclops.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RampagingCyclops());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(cyclops));
        blocker.addBlockingTargetId(cyclops.getId());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Rampaging Cyclops gets -2/-0 when blocked by two creatures")
    void reducedPowerWhenBlockedByTwo() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new RampagingCyclops());
        cyclops.setSummoningSick(false);
        cyclops.setAttacking(true);

        int cyclopsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cyclops);

        Permanent blocker1 = harness.addToBattlefieldAndReturn(player2, new RampagingCyclops());
        blocker1.setSummoningSick(false);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(cyclopsIndex);
        blocker1.addBlockingTargetId(cyclops.getId());

        Permanent blocker2 = harness.addToBattlefieldAndReturn(player2, new RampagingCyclops());
        blocker2.setSummoningSick(false);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(cyclopsIndex);
        blocker2.addBlockingTargetId(cyclops.getId());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Rampaging Cyclops gets -2/-0 when blocked by three creatures")
    void reducedPowerWhenBlockedByThree() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new RampagingCyclops());
        cyclops.setSummoningSick(false);
        cyclops.setAttacking(true);

        int cyclopsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cyclops);

        for (int i = 0; i < 3; i++) {
            Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RampagingCyclops());
            blocker.setSummoningSick(false);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(cyclopsIndex);
            blocker.addBlockingTargetId(cyclops.getId());
        }

        // The penalty is a flat -2, not per blocker.
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power returns immediately when one of two blockers leaves the battlefield")
    void powerReturnsWhenBlockerLeaves() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new RampagingCyclops());
        cyclops.setAttacking(true);
        Permanent blocker1 = harness.addToBattlefieldAndReturn(player2, new RampagingCyclops());
        Permanent blocker2 = harness.addToBattlefieldAndReturn(player2, new RampagingCyclops());
        for (Permanent blocker : java.util.List.of(blocker1, blocker2)) {
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            blocker.addBlockingTargetId(cyclops.getId());
        }

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(2);
        gd.playerBattlefields.get(player2.getId()).remove(blocker1);
        gd.playerGraveyards.get(player2.getId()).add(blocker1.getCard());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures blocking another attacker do not reduce this Cyclops's power")
    void onlyCountsItsOwnBlockers() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new RampagingCyclops());
        Permanent otherCyclops = harness.addToBattlefieldAndReturn(player1, new RampagingCyclops());
        cyclops.setAttacking(true);
        otherCyclops.setAttacking(true);
        for (int i = 0; i < 2; i++) {
            Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RampagingCyclops());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(1);
            blocker.addBlockingTargetId(otherCyclops.getId());
        }

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherCyclops)).isEqualTo(2);
    }
}
