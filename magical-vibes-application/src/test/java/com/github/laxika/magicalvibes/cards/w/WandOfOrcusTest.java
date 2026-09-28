package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieAssassin;
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

@CardUsed({WandOfOrcus.class, GrizzlyBears.class, ZombieAssassin.class})
class WandOfOrcusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the equipped creature grants it and controlled Zombies deathtouch until end of turn")
    void attackingGrantsDeathtouchToEquippedCreatureAndZombies() {
        Permanent wand = addWandReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player1, new ZombieAssassin());
        Permanent nonZombie = addCreatureReady(player1, new GrizzlyBears());
        wand.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonZombie, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Blocking with the equipped creature grants it and controlled Zombies deathtouch until end of turn")
    void blockingGrantsDeathtouchToEquippedCreatureAndZombies() {
        Permanent wand = addWandReady(player2);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player2, new ZombieAssassin());
        wand.setAttachedTo(blocker.getId());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Combat damage to a player creates that many 2/2 black Zombie tokens")
    void combatDamageCreatesZombieTokensEqualToDamage() {
        Permanent wand = addWandReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        wand.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(zombies).hasSize(2);
        assertThat(zombies).allSatisfy(zombie -> {
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        });
    }

    private Permanent addWandReady(Player player) {
        Permanent wand = new Permanent(new WandOfOrcus());
        wand.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wand);
        return wand;
    }
}
