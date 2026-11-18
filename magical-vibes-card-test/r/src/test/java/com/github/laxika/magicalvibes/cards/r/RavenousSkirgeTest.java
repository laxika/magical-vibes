package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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

        assertThat(PermanentGrants.powerBoostOf(gd, skirge)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, skirge)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the Ravenous Skirge that attacks gets the attack boost")
    void onlyAttackingSkirgeGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new RavenousSkirge());
        Permanent nonAttacker = addCreatureReady(player1, new RavenousSkirge());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, nonAttacker)).isZero();
    }

    @Test
    @DisplayName("Attack boost waits for its triggered ability to resolve")
    void boostWaitsForResolution() {
        Permanent skirge = addCreatureReady(player1, new RavenousSkirge());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, skirge)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, skirge)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, skirge)).isZero();
    }

    @Test
    @DisplayName("Each attacking Skirge gets its own boost without boosting an opposing Skirge")
    void simultaneousAttackersEachGetOneBoost() {
        Permanent first = addCreatureReady(player1, new RavenousSkirge());
        Permanent second = addCreatureReady(player1, new RavenousSkirge());
        Permanent opposing = addCreatureReady(player2, new RavenousSkirge());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposing)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposing)).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent skirge = addCreatureReady(player1, new RavenousSkirge());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, skirge)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, skirge)).isEqualTo(0);
    }
}
