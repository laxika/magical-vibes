package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoldierReplicaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Soldier Replica deals 3 damage to an attacking creature")
    void damagesAttackingCreature() {
        addReadySoldierReplica(player1);
        Permanent target = addCombatCreature(player2, true, false);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soldier Replica");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void damagesBlockingCreature() {
        addReadySoldierReplica(player1);
        Permanent target = addCombatCreature(player2, false, true);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soldier Replica");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addReadySoldierReplica(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadySoldierReplica(player1);
        Permanent target = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addReadySoldierReplica(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new SoldierReplica());
    }

    private Permanent addCombatCreature(com.github.laxika.magicalvibes.model.Player player,
                                         boolean attacking,
                                         boolean blocking) {
        Permanent target = addCreatureReady(player, new GrizzlyBears());
        target.setAttacking(attacking);
        target.setBlocking(blocking);
        return target;
    }
}
