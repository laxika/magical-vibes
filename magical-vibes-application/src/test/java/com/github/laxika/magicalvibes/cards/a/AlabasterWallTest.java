package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CinderElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlabasterWall.class, CinderElemental.class})
class AlabasterWallTest extends BaseCardTest {

    @Test
    @DisplayName("The ability taps Alabaster Wall and prevents damage to a target creature")
    void preventsDamageToTargetCreature() {
        Permanent wall = addReadyWall(player1);
        Permanent target = addCreatureReady(player2, new CinderElemental());

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
    @DisplayName("The shield prevents the next damage dealt to the target creature")
    void preventsNextDamageToTargetCreature() {
        Permanent wall = addReadyWall(player1);
        addCreatureReady(player2, new CinderElemental());

        activateWallAbility(wall, wall.getId());

        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 2, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The shield prevents the next damage dealt to the target player")
    void preventsNextDamageToTargetPlayer() {
        addReadyWall(player1);
        addCreatureReady(player2, new CinderElemental());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The shield prevents only one damage and is then consumed")
    void preventsOnlyTheNextDamage() {
        Permanent wall = addReadyWall(player1);
        addCreatureReady(player2, new CinderElemental());
        addCreatureReady(player2, new CinderElemental());

        activateWallAbility(wall, wall.getId());

        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, 2, wall.getId());
        harness.activateAbility(player2, 0, 2, wall.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("An unused shield expires at cleanup")
    void unusedShieldExpiresAtCleanup() {
        Permanent wall = addReadyWall(player1);

        activateWallAbility(wall, wall.getId());
        assertThat(wall.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wall.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The ability requires a target")
    void requiresTarget() {
        addReadyWall(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWall(Player player) {
        return addCreatureReady(player, new AlabasterWall());
    }

    private void activateWallAbility(Permanent wall, UUID targetId) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(wall), null, targetId);
        harness.passBothPriorities();
    }
}
