package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodfireKavu;
import com.github.laxika.magicalvibes.cards.h.HuntingKavu;
import com.github.laxika.magicalvibes.cards.k.KavuClimber;
import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShorelineRaider.class, BloodfireKavu.class, KavuClimber.class, MetathranZombie.class,
        HuntingKavu.class})
class ShorelineRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Shoreline Raider takes no damage from Bloodfire Kavu")
    void takesNoDamageFromKavu() {
        addCreatureReady(player1, new BloodfireKavu());
        Permanent raider = addCreatureReady(player2, new ShorelineRaider());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Shoreline Raider takes normal combat damage from a non-Kavu creature")
    void takesNormalDamageFromNonKavu() {
        Permanent attacker = addCreatureReady(player1, new MetathranZombie());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ShorelineRaider());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kavu creature cannot block Shoreline Raider")
    void kavuCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ShorelineRaider());
        attacker.setAttacking(true);

        addCreatureReady(player2, new KavuClimber());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Kavu creature can block Shoreline Raider")
    void nonKavuCanBlock() {
        Permanent attacker = addCreatureReady(player1, new ShorelineRaider());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MetathranZombie());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shoreline Raider cannot be targeted by a Kavu ability")
    void kavuAbilityCannotTargetShorelineRaider() {
        Permanent kavu = addCreatureReady(player1, new HuntingKavu());
        Permanent attacker = addCreatureReady(player2, new ShorelineRaider());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(kavu.isTapped()).isFalse();
    }
}
