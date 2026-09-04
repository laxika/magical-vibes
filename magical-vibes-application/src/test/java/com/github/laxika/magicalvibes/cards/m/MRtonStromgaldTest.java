package com.github.laxika.magicalvibes.cards.m;

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

        assertThat(marton.getPowerModifier()).isZero();
        assertThat(marton.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking with two others gives each other attacker +2/+2, none to itself")
    void attackingWithTwoOthersBoostsOthers() {
        Permanent marton = addCreatureReady(player1, new MRtonStromgald());
        Permanent bearA = addCreatureReady(player1, new BalduvianBears());
        Permanent bearB = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(bearA.getPowerModifier()).isEqualTo(2);
        assertThat(bearA.getToughnessModifier()).isEqualTo(2);
        assertThat(bearB.getPowerModifier()).isEqualTo(2);
        assertThat(bearB.getToughnessModifier()).isEqualTo(2);
        assertThat(marton.getPowerModifier()).isZero();
        assertThat(marton.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Non-attacking creatures are not boosted")
    void nonAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new MRtonStromgald());
        Permanent attackingBear = addCreatureReady(player1, new BalduvianBears());
        Permanent homeBear = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(attackingBear.getPowerModifier()).isEqualTo(1);
        assertThat(homeBear.getPowerModifier()).isZero();
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

        assertThat(firstBear.getPowerModifier()).isEqualTo(2);
        assertThat(firstBear.getToughnessModifier()).isEqualTo(2);
        assertThat(secondBear.getPowerModifier()).isEqualTo(2);
        assertThat(secondBear.getToughnessModifier()).isEqualTo(2);
        assertThat(marton.getPowerModifier()).isZero();
        assertThat(marton.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attack boost wears off at end of turn")
    void attackBoostWearsOff() {
        addCreatureReady(player1, new MRtonStromgald());
        Permanent bear = addCreatureReady(player1, new BalduvianBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(bear.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
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

        assertThat(blockingBear.getPowerModifier()).isEqualTo(1);
        assertThat(blockingBear.getToughnessModifier()).isEqualTo(1);
        assertThat(marton.getPowerModifier()).isZero();
        assertThat(marton.getToughnessModifier()).isZero();
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

        assertThat(marton.getPowerModifier()).isZero();
        assertThat(marton.getToughnessModifier()).isZero();
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

        assertThat(firstBlocker.getPowerModifier()).isEqualTo(2);
        assertThat(firstBlocker.getToughnessModifier()).isEqualTo(2);
        assertThat(secondBlocker.getPowerModifier()).isEqualTo(2);
        assertThat(secondBlocker.getToughnessModifier()).isEqualTo(2);
        assertThat(marton.getPowerModifier()).isZero();
        assertThat(marton.getToughnessModifier()).isZero();
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

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(reenteredMarton.getPowerModifier()).isEqualTo(2);
        assertThat(reenteredMarton.getToughnessModifier()).isEqualTo(2);
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

        assertThat(firstBlocker.getPowerModifier()).isEqualTo(2);
        assertThat(firstBlocker.getToughnessModifier()).isEqualTo(2);
        assertThat(reenteredMarton.getPowerModifier()).isEqualTo(2);
        assertThat(reenteredMarton.getToughnessModifier()).isEqualTo(2);
    }
}
