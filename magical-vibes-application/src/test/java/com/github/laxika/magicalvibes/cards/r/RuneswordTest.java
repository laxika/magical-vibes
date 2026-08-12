package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuneswordTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the targeted attacking creature")
    void boostsTargetAttackingCreature() {
        Permanent sword = addSwordReady();
        Permanent attacker = addAttacker();
        int basePower = gqs.getEffectivePower(gd, attacker);

        activate(sword, attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(basePower + 2);
        assertThat(sword.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifices itself when the targeted creature leaves")
    void sacrificesWhenTargetLeaves() {
        Permanent sword = addSwordReady();
        Permanent attacker = addAttacker();

        activate(sword, attacker);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, attacker));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runesword");
    }

    @Test
    @DisplayName("Exiles a creature damaged by the targeted creature instead of letting it regenerate")
    void exilesCreatureDamagedByTarget() {
        Permanent sword = addSwordReady();
        Permanent attacker = addAttacker();
        Permanent skeletons = new Permanent(new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);
        gd.playerBattlefields.get(player2.getId()).add(skeletons);

        activate(sword, attacker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertNotInGraveyard(player2, "Drudge Skeletons");
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Drudge Skeletons"))).isTrue();
    }

    @Test
    @DisplayName("Requires an attacking creature as its target")
    void requiresAttackingCreature() {
        Permanent sword = addSwordReady();
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sword), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSwordReady() {
        Permanent sword = new Permanent(new Runesword());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sword);
        return sword;
    }

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private void activate(Permanent sword, Permanent attacker) {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(sword), 0, null, attacker.getId());
        harness.passBothPriorities();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
