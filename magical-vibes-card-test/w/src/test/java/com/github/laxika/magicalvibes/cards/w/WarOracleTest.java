package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarOracle.class, YokedOx.class})
class WarOracleTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to a player makes it renowned and gains life from lifelink")
    void becomesRenownedAndGainsLife() {
        Permanent oracle = addCreatureReady(player1, new WarOracle());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        attackUnblocked();

        assertThat(oracle.isRenowned()).isTrue();
        assertThat(oracle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 3);
    }

    @Test
    @DisplayName("Renown only applies once")
    void renownAppliesOnce() {
        Permanent oracle = addCreatureReady(player1, new WarOracle());
        oracle.setRenowned(true);

        attackUnblocked();

        assertThat(oracle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A blocked War Oracle never becomes renowned but still gains life")
    void blockedDoesNotBecomeRenowned() {
        Permanent oracle = addCreatureReady(player1, new WarOracle());
        addCreatureReady(player2, new YokedOx());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(oracle.isRenowned()).isFalse();
        assertThat(oracle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 3);
    }

    @Test
    @DisplayName("A second attack gains life for the increased power without adding another renown counter")
    void secondAttackUsesRenownCounterForLifelink() {
        Permanent oracle = addCreatureReady(player1, new WarOracle());
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        attackUnblocked();
        harness.performUntapStep(player1);
        attackUnblocked();

        assertThat(oracle.isRenowned()).isTrue();
        assertThat(oracle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 7);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLife - 7);
    }

    @Test
    @DisplayName("Dealing damage as a blocker gains life without making War Oracle renowned")
    void blockingGainsLifeWithoutRenown() {
        addCreatureReady(player1, new YokedOx());
        Permanent oracle = addCreatureReady(player2, new WarOracle());
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(oracle.isRenowned()).isFalse();
        assertThat(oracle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife + 3);
    }

    private void attackUnblocked() {
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
    }
}
