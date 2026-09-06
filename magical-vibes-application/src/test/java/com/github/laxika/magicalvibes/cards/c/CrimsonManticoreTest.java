package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrimsonManticore.class, MonssGoblinRaiders.class, GrizzlyBears.class})
class CrimsonManticoreTest extends BaseCardTest {

    private Permanent addReadyManticore() {
        return addCreatureReady(player1, new CrimsonManticore());
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new MonssGoblinRaiders());
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, Permanent attacker) {
        Permanent blocker = addCreatureReady(owner, new MonssGoblinRaiders());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        return blocker;
    }

    @Test
    @DisplayName("Deals 1 damage to a target attacking creature")
    void damagesAttacker() {
        addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        // 1 damage kills the 1/1 attacker
        harness.assertNotOnBattlefield(player2, "Mons's Goblin Raiders");
        harness.assertInGraveyard(player2, "Mons's Goblin Raiders");
    }

    @Test
    @DisplayName("Deals 1 damage to a target blocking creature")
    void damagesBlocker() {
        addReadyManticore();
        Permanent attacker = addAttacker(player1);
        Permanent blocker = addBlocker(player2, attacker);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mons's Goblin Raiders");
        harness.assertInGraveyard(player2, "Mons's Goblin Raiders");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addReadyManticore();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires tap — cannot activate if already tapped")
    void cannotActivateIfTapped() {
        Permanent manticore = addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        manticore.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires one red mana to activate")
    void cannotActivateWithOnlyColorlessMana() {
        addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Consumes one red mana when activated")
    void consumesRedMana() {
        addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not damage a target that stops attacking before resolution")
    void targetBecomesNonCombatCreatureBeforeResolution() {
        addReadyManticore();
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mons's Goblin Raiders");
    }
}
