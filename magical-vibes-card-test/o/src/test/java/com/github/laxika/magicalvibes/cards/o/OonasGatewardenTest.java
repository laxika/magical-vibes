package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.t.ThornwatchScarecrow;
import com.github.laxika.magicalvibes.cards.w.WaspLancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OonasGatewarden.class, ThornwatchScarecrow.class, WaspLancer.class})
class OonasGatewardenTest extends BaseCardTest {

    @Test
    void defenderPreventsAttackingEvenWhenNotSummoningSick() {
        Permanent gatewarden = addCreatureReady(player1, new OonasGatewarden());

        assertThat(als.canAttack(gd, gatewarden, player1.getId())).isFalse();
    }

    @Test
    void flyingAllowsBlockingAFlyerAndWitherKillsIt() {
        Permanent attacker = addCreatureReady(player1, new WaspLancer());
        attacker.setAttacking(true);
        addCreatureReady(player2, new OonasGatewarden());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Wasp Lancer");
        harness.assertInGraveyard(player2, "Oona's Gatewarden");
        harness.assertLife(player2, 20);
    }

    @Test
    void witherFromABlockerLeavesCountersOnASurvivingGroundAttacker() {
        Permanent attacker = addCreatureReady(player1, new ThornwatchScarecrow());
        attacker.setAttacking(true);
        addCreatureReady(player2, new OonasGatewarden());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        Permanent survivor = findPermanent(player1, "Thornwatch Scarecrow");
        assertThat(survivor.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(survivor.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Oona's Gatewarden");
        harness.assertLife(player2, 20);
    }
}
