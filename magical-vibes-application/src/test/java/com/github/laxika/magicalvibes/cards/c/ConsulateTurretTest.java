package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyTurret() {
        return harness.addToBattlefieldAndReturn(player1, new ConsulateTurret());
    }
}
