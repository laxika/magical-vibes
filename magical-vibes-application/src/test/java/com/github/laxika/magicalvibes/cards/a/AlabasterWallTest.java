package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlabasterWallTest extends BaseCardTest {

    @Test
    @DisplayName("The ability taps Alabaster Wall and prevents damage to a target creature")
    void preventsDamageToTargetCreature() {
        Permanent wall = addReadyWall(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(wall.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(gd.globalDamagePreventionShield).isZero();
    }

    @Test
    @DisplayName("The ability can prevent damage to a target player")
    void preventsDamageToTargetPlayer() {
        addReadyWall(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability requires a target")
    void requiresTarget() {
        addReadyWall(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWall(Player player) {
        Permanent wall = harness.addToBattlefieldAndReturn(player, new AlabasterWall());
        wall.setSummoningSick(false);
        return wall;
    }
}
