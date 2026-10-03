package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TezzeretTheSchemer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsulateTurret.class, GrizzlyBears.class, TezzeretTheSchemer.class})
class ConsulateTurretTest extends BaseCardTest {

    @Test
    void tapsToGetAnEnergyCounter() {
        Permanent turret = addReadyTurret();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(turret.isTapped()).isTrue();
    }

    @Test
    void paysThreeEnergyAndTapsToDealTwoDamageToPlayer() {
        Permanent turret = addReadyTurret();
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(turret.isTapped()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotActivateDamageAbilityWithoutThreeEnergyCounters() {
        addReadyTurret();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    @Test
    void damageAbilityCannotTargetAcreature() {
        addReadyTurret();
        gd.playerEnergyCounters.put(player1.getId(), 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void energyIsGainedOnResolutionRatherThanActivation() {
        Permanent turret = addReadyTurret();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(turret.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void energyIsPaidImmediatelyAndOnlyOnce() {
        Permanent turret = addReadyTurret();
        gd.playerEnergyCounters.put(player1.getId(), 7);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(turret.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        harness.assertLife(player2, 18);
    }

    @Test
    void damageAbilityCanTargetItsController() {
        addReadyTurret();
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void damageAbilityRemovesTwoLoyaltyFromPlaneswalker() {
        addReadyTurret();
        gd.playerEnergyCounters.put(player1.getId(), 3);
        Permanent target = harness.enterBattlefieldAndReturn(player2, new TezzeretTheSchemer());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainingEnergyPreventsUsingDamageAbilityUntilTurretUntaps() {
        addReadyTurret();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    private Permanent addReadyTurret() {
        return harness.addToBattlefieldAndReturn(player1, new ConsulateTurret());
    }
}
