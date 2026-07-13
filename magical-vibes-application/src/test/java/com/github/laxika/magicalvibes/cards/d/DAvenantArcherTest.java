package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DAvenantArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to attacking creature and kills a 1-toughness one")
    void abilityDamagesAttacker() {
        Permanent archer = addArcherReady(player1);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        assertThat(archer.isTapped()).isTrue();
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Ability can target a blocking creature")
    void abilityDamagesBlocker() {
        addArcherReady(player1);
        Permanent blocker = addCombatCreature(player2, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetIdleCreature() {
        addArcherReady(player1);
        Permanent idle = addCombatCreature(player2, false, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Cannot activate while D'Avenant Archer has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent archer = new Permanent(new DAvenantArcher());
        gd.playerBattlefields.get(player1.getId()).add(archer);
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private Permanent addArcherReady(Player player) {
        Permanent archer = new Permanent(new DAvenantArcher());
        archer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(archer);
        return archer;
    }

    private Permanent addCombatCreature(Player player, boolean attacking, boolean blocking) {
        Permanent creature = new Permanent(new FugitiveWizard());
        creature.setSummoningSick(false);
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
