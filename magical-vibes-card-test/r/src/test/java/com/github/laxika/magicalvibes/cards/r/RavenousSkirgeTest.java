package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RavenousSkirge.class)
class RavenousSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent skirge = addCreatureReady(player1, new RavenousSkirge());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(skirge.getPowerModifier()).isEqualTo(2);
        assertThat(skirge.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the Ravenous Skirge that attacks gets the attack boost")
    void onlyAttackingSkirgeGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new RavenousSkirge());
        Permanent nonAttacker = addCreatureReady(player1, new RavenousSkirge());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(nonAttacker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Attack boost waits for its triggered ability to resolve")
    void boostWaitsForResolution() {
        Permanent skirge = addCreatureReady(player1, new RavenousSkirge());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(skirge.getPowerModifier()).isZero();

        resolveAllTriggers();

        assertThat(skirge.getPowerModifier()).isEqualTo(2);
        assertThat(skirge.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking Skirge gets its own boost without boosting an opposing Skirge")
    void simultaneousAttackersEachGetOneBoost() {
        Permanent first = addCreatureReady(player1, new RavenousSkirge());
        Permanent second = addCreatureReady(player1, new RavenousSkirge());
        Permanent opposing = addCreatureReady(player2, new RavenousSkirge());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(opposing.getPowerModifier()).isZero();
        assertThat(opposing.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent skirge = addCreatureReady(player1, new RavenousSkirge());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(skirge.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skirge.getPowerModifier()).isEqualTo(0);
    }
}
