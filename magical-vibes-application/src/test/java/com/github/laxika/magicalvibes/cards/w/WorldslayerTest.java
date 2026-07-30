package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorldslayerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player destroys every permanent except Worldslayer itself")
    void combatDamageWipesEverythingButWorldslayer() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(land);
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent worldslayer = addWorldslayerReady(player1);
        worldslayer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(worldslayer);
    }

    private Permanent addWorldslayerReady(Player player) {
        Permanent perm = new Permanent(new Worldslayer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
