package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MRtonStromgald.class, BalduvianBears.class})
class MRtonStromgaldTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone gives no boost to anyone")
    void attackingAloneDoesNothing() {
        Permanent marton = addCreatureReady(player1, new MRtonStromgald());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, marton)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, marton)).isZero();
    }

    @Test
    @DisplayName("Attacking with two others gives each other attacker +2/+2, none to itself")
    void attackingWithTwoOthersBoostsOthers() {
        Permanent marton = addCreatureReady(player1, new MRtonStromgald());
        Permanent bearA = addCreatureReady(player1, new BalduvianBears());
        Permanent bearB = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bearA)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bearA)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, bearB)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bearB)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, marton)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, marton)).isZero();
    }

    @Test
    @DisplayName("Non-attacking creatures are not boosted")
    void nonAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new MRtonStromgald());
        Permanent attackingBear = addCreatureReady(player1, new BalduvianBears());
        Permanent homeBear = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attackingBear)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, homeBear)).isZero();
    }

    @Test
    @DisplayName("Attack boost counts attackers present when the trigger resolves")
    void attackBoostCountsAttackersAtResolution() {
        Permanent marton = addCreatureReady(player1, new MRtonStromgald());
        Permanent firstBear = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1));

        Permanent secondBear = addCreatureReady(player1, new BalduvianBears());
        secondBear.setAttacking(true);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, firstBear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstBear)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, secondBear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondBear)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, marton)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, marton)).isZero();
    }

    @Test
    @DisplayName("Attack boost wears off at end of turn")
    void attackBoostWearsOff() {
        addCreatureReady(player1, new MRtonStromgald());
        Permanent bear = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isZero();
    }

    @Test
    @DisplayName("Blocking alongside another blocker boosts that blocker, not itself")
    void blockingBoostsOtherBlockers() {
        Permanent attackerA = addCreatureReady(player1, new BalduvianBears());
        attackerA.setAttacking(true);
        Permanent attackerB = addCreatureReady(player1, new BalduvianBears());
        attackerB.setAttacking(true);

        Permanent marton = addCreatureReady(player2, new MRtonStromgald());
        Permanent blockingBear = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, blockingBear)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blockingBear)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, marton)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, marton)).isZero();
    }

    @Test
    @DisplayName("Blocking alone gives no boost")
    void blockingAloneDoesNothing() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent marton = addCreatureReady(player2, new MRtonStromgald());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, marton)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, marton)).isZero();
    }

    @Test
    @DisplayName("Block boost counts blockers present when the trigger resolves")
    void blockBoostCountsBlockersAtResolution() {
        Permanent attackerA = addCreatureReady(player1, new BalduvianBears());
        attackerA.setAttacking(true);
        Permanent attackerB = addCreatureReady(player1, new BalduvianBears());
        attackerB.setAttacking(true);

        Permanent marton = addCreatureReady(player2, new MRtonStromgald());
        Permanent firstBlocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        Permanent secondBlocker = addCreatureReady(player2, new BalduvianBears());
        secondBlocker.setBlocking(true);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firstBlocker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstBlocker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, secondBlocker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondBlocker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, marton)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, marton)).isZero();
    }

    @Test
    @DisplayName("A re-entered Márton is another attacker for the old trigger")
    void reenteredMartonIsAnotherAttackerForOldTrigger() {
        Permanent marton = addCreatureReady(player1, new MRtonStromgald());
        Permanent bear = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1));

        gd.playerBattlefields.get(player1.getId()).remove(marton);
        Permanent reenteredMarton = addCreatureReady(player1, marton.getCard());
        reenteredMarton.setAttacking(true);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, reenteredMarton)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, reenteredMarton)).isEqualTo(2);
    }

    @Test
    @DisplayName("A re-entered Márton is another blocker for the old trigger")
    void reenteredMartonIsAnotherBlockerForOldTrigger() {
        Permanent attackerA = addCreatureReady(player1, new BalduvianBears());
        attackerA.setAttacking(true);
        Permanent attackerB = addCreatureReady(player1, new BalduvianBears());
        attackerB.setAttacking(true);

        Permanent marton = addCreatureReady(player2, new MRtonStromgald());
        Permanent firstBlocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        gd.playerBattlefields.get(player2.getId()).remove(marton);
        Permanent reenteredMarton = addCreatureReady(player2, marton.getCard());
        reenteredMarton.setBlocking(true);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, firstBlocker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstBlocker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, reenteredMarton)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, reenteredMarton)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocking multiple attackers triggers the boost only once")
    void blockingMultipleAttackersBoostsOnlyOnce() {
        addCreatureReady(player1, new BalduvianBears()).setAttacking(true);
        addCreatureReady(player1, new BalduvianBears()).setAttacking(true);
        addCreatureReady(player1, new BalduvianBears()).setAttacking(true);
        Permanent marton = addCreatureReady(player2, new MRtonStromgald());
        gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("test", null, null,
                new GrantAdditionalBlockToTargetUntilEndOfTurnEffect(1), marton.getId(), Duration.UNTIL_END_OF_TURN));
        Permanent bear = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 2)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, marton)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, marton)).isZero();
    }

    @Test
    @DisplayName("Creatures removed from combat before resolution are neither counted nor boosted")
    void removedAttackerIsNotCountedOrBoosted() {
        addCreatureReady(player1, new MRtonStromgald());
        Permanent remaining = addCreatureReady(player1, new BalduvianBears());
        Permanent removed = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1, 2));
        removed.setAttacking(false);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, remaining)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, remaining)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, removed)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, removed)).isZero();
    }

    @Test
    @DisplayName("Resolved boosts stay fixed and do not apply to later attackers")
    void resolvedBoostDoesNotTrackCombatChanges() {
        addCreatureReady(player1, new MRtonStromgald());
        Permanent bear = addCreatureReady(player1, new BalduvianBears());
        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        bear.setAttacking(false);
        Permanent laterAttacker = addCreatureReady(player1, new BalduvianBears());
        laterAttacker.setAttacking(true);

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, laterAttacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, laterAttacker)).isZero();
    }
}
