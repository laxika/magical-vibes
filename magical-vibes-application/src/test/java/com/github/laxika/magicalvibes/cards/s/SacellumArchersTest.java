package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SacellumArchers.class, GrizzlyBears.class})
class SacellumArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target attacking creature, destroying a 2/2")
    void deals2DamageToAttacker() {
        Permanent archers = addReadyArchers(player1);
        payManaCost(player1);
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(archers.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to a target blocking creature")
    void deals2DamageToBlocker() {
        addReadyArchers(player1);
        payManaCost(player1);
        Permanent blocker = addBlocker(player2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addReadyArchers(player1);
        payManaCost(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SacellumArchers());
        payManaCost(player1);
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent archers = addReadyArchers(player1);
        archers.tap();
        payManaCost(player1);
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayWithOnlyRedMana() {
        Permanent archers = addReadyArchers(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(archers.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayWithOnlyWhiteMana() {
        Permanent archers = addReadyArchers(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(archers.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDamageTargetThatLeavesCombatBeforeResolution() {
        Permanent archers = addReadyArchers(player1);
        payManaCost(player1);
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(archers.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent archers = addReadyArchers(player1);
        payManaCost(player1);
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(archers);
        gd.playerGraveyards.get(player1.getId()).add(archers.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItsControllersAttackingCreature() {
        Permanent archers = addReadyArchers(player1);
        payManaCost(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(archers.isTapped()).isTrue();
    }

    private void payManaCost(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private Permanent addReadyArchers(Player player) {
        return addCreatureReady(player, new SacellumArchers());
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = addCreatureReady(owner, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
