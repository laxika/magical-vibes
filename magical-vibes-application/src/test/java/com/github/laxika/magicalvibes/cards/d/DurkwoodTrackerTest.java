package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DurkwoodTracker.class, GrizzlyBears.class})
class DurkwoodTrackerTest extends BaseCardTest {

    @Test
    void fightsTargetAttackingCreature() {
        Permanent tracker = addReadyTracker(player1);
        Permanent attacker = addAttackingCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(tracker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonAttackingCreature() {
        addReadyTracker(player1);
        Permanent creature = addCreature(player2);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNothingIfTrackerLeavesBeforeResolution() {
        Permanent tracker = addReadyTracker(player1);
        Permanent attacker = addAttackingCreature(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tracker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private Permanent addReadyTracker(Player player) {
        Permanent tracker = new Permanent(new DurkwoodTracker());
        tracker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tracker);
        return tracker;
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreature(player);
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
