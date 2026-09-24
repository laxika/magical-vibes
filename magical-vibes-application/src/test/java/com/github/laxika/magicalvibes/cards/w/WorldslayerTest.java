package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Worldslayer.class, AlphaMyr.class, Forest.class})
class WorldslayerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player destroys every permanent except Worldslayer itself")
    void combatDamageWipesEverythingButWorldslayer() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent enemy = addCreatureReady(player2, new AlphaMyr());
        Permanent worldslayer = addWorldslayerReady(player1);
        worldslayer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(worldslayer);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enemy);
    }

    @Test
    @DisplayName("No wipe when the equipped creature is blocked and deals no damage to a player")
    void noWipeWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent worldslayer = addWorldslayerReady(player1);
        worldslayer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AlphaMyr());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(worldslayer, land);
    }

    @Test
    @DisplayName("Equip ability costs five mana and attaches Worldslayer to a creature")
    void equipCostsFiveManaAndAttaches() {
        Permanent worldslayer = addWorldslayerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(worldslayer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addWorldslayerReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Worldslayer());
        perm.setSummoningSick(false);
        return perm;
    }
}
