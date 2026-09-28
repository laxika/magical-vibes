package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieCutthroat;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WandOfOrcus.class, GrizzlyBears.class, ZombieCutthroat.class})
class WandOfOrcusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the equipped creature grants deathtouch to it and your Zombies")
    void attackGrantsDeathtouchToEquippedCreatureAndZombies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player1, new ZombieCutthroat());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Blocking with the equipped creature grants deathtouch to it and your Zombies")
    void blockGrantsDeathtouchToEquippedCreatureAndZombies() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player1, new ZombieCutthroat());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        declareAttackers(player2, java.util.List.of(0));
        prepareDeclareBlockers(player2);
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        gs.declareBlockers(gd, player1, java.util.List.of(new BlockerAssignment(blockerIndex, 0)));
        resolveAllTriggers();

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Combat damage from the equipped creature creates that many Zombies")
    void combatDamageCreatesThatManyZombies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    private Permanent addWandReady(Player player) {
        Permanent wand = new Permanent(new WandOfOrcus());
        wand.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wand);
        return wand;
    }
}
