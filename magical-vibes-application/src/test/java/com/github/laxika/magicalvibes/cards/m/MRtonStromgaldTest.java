package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MRtonStromgaldTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone gives no boost to anyone")
    void attackingAloneDoesNothing() {
        Permanent marton = addReadyMarton(player1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(marton.getPowerModifier()).isZero();
        assertThat(marton.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking with two others gives each other attacker +2/+2, none to itself")
    void attackingWithTwoOthersBoostsOthers() {
        Permanent marton = addReadyMarton(player1);
        Permanent bearA = addReadyBear(player1);
        Permanent bearB = addReadyBear(player1);

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
        addReadyMarton(player1);
        Permanent attackingBear = addReadyBear(player1);
        Permanent homeBear = addReadyBear(player1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(attackingBear.getPowerModifier()).isEqualTo(1);
        assertThat(homeBear.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Attack boost wears off at end of turn")
    void attackBoostWearsOff() {
        addReadyMarton(player1);
        Permanent bear = addReadyBear(player1);

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
        Permanent attackerA = addReadyBear(player1);
        attackerA.setAttacking(true);
        Permanent attackerB = addReadyBear(player1);
        attackerB.setAttacking(true);

        Permanent marton = addReadyMarton(player2);
        Permanent blockingBear = addReadyBear(player2);

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
        Permanent attacker = addReadyBear(player1);
        attacker.setAttacking(true);

        Permanent marton = addReadyMarton(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(marton.getPowerModifier()).isZero();
        assertThat(marton.getToughnessModifier()).isZero();
    }

    private Permanent addReadyMarton(Player player) {
        Permanent permanent = new Permanent(new MRtonStromgald());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBear(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
