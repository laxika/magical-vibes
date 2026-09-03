package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Carnophage;
import com.github.laxika.magicalvibes.cards.r.RobeOfMirrors;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SongOfSerenity.class, Carnophage.class, RobeOfMirrors.class})
class SongOfSerenityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack while Song of Serenity is on the battlefield")
    void enchantedCreatureCannotAttack() {
        harness.addToBattlefield(player1, new SongOfSerenity());
        Permanent enchanted = addCreatureReady(player1, new Carnophage());
        attachRobeOfMirrors(enchanted, player2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(enchanted);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unenchanted creature can attack while Song of Serenity is on the battlefield")
    void unenchantedCreatureCanAttack() {
        harness.addToBattlefield(player1, new SongOfSerenity());
        harness.setLife(player2, 20);
        Permanent unenchanted = addCreatureReady(player1, new Carnophage());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(unenchanted);
        declareAttackers(player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Enchanted creature cannot block while Song of Serenity is on the battlefield")
    void enchantedCreatureCannotBlock() {
        harness.addToBattlefield(player1, new SongOfSerenity());
        Permanent attacker = addCreatureReady(player1, new Carnophage());
        attacker.setAttacking(true);
        Permanent enchanted = addCreatureReady(player2, new Carnophage());
        attachRobeOfMirrors(enchanted, player1);

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(enchanted);
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unenchanted creature can block while Song of Serenity is on the battlefield")
    void unenchantedCreatureCanBlock() {
        harness.addToBattlefield(player1, new SongOfSerenity());
        Permanent attacker = addCreatureReady(player1, new Carnophage());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Carnophage());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can attack after Song of Serenity leaves the battlefield")
    void restrictionLiftsWhenSongOfSerenityLeaves() {
        Permanent song = new Permanent(new SongOfSerenity());
        gd.playerBattlefields.get(player1.getId()).add(song);
        harness.setLife(player2, 20);
        Permanent enchanted = addCreatureReady(player1, new Carnophage());
        attachRobeOfMirrors(enchanted, player2);

        gd.playerBattlefields.get(player1.getId()).remove(song);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(enchanted);
        declareAttackers(player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void attachRobeOfMirrors(Permanent creature, Player controller) {
        Permanent aura = new Permanent(new RobeOfMirrors());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

}
