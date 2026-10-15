package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DeathWind;
import com.github.laxika.magicalvibes.cards.f.FalkenrathExterminator;
import com.github.laxika.magicalvibes.cards.h.HavengulVampire;
import com.github.laxika.magicalvibes.cards.u.UndeadExecutioner;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MidnightDuelist.class, HavengulVampire.class, UndeadExecutioner.class,
        FalkenrathExterminator.class, DeathWind.class})
class MidnightDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Midnight Duelist takes no combat damage from a blocked Vampire attacker")
    void takesNoDamageFromVampire() {
        Permanent attacker = addCreatureReady(player1, new HavengulVampire());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MidnightDuelist());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Midnight Duelist");
        harness.assertOnBattlefield(player1, "Havengul Vampire");
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Midnight Duelist dies to combat damage from a non-Vampire attacker")
    void takesNormalDamageFromNonVampire() {
        Permanent attacker = addCreatureReady(player1, new UndeadExecutioner());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MidnightDuelist());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Midnight Duelist");
    }

    @Test
    @DisplayName("A Vampire cannot block Midnight Duelist")
    void vampireCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new MidnightDuelist());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HavengulVampire());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A non-Vampire creature can block Midnight Duelist")
    void nonVampireCanBlock() {
        Permanent attacker = addCreatureReady(player1, new MidnightDuelist());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UndeadExecutioner());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void opposingVampireCannotTargetDuelist() {
        Permanent source = addCreatureReady(player1, new FalkenrathExterminator());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent duelist = harness.addToBattlefieldAndReturn(player2, new MidnightDuelist());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, duelist.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(gd.stack).isEmpty();
        assertThat(duelist.getMarkedDamage()).isZero();
    }

    @Test
    void friendlyVampireCannotTargetDuelist() {
        Permanent source = addCreatureReady(player1, new FalkenrathExterminator());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent duelist = harness.addToBattlefieldAndReturn(player1, new MidnightDuelist());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, duelist.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonVampireBlackSpellCanTargetAndKillDuelist() {
        Permanent duelist = harness.addToBattlefieldAndReturn(player2, new MidnightDuelist());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 2, duelist.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Midnight Duelist");
        harness.assertInGraveyard(player2, "Midnight Duelist");
    }
}
