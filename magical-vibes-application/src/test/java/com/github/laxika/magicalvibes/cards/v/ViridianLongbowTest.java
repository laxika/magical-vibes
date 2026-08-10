package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JackalPup;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViridianLongbowTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Viridian Longbow to a creature")
    void equipAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent longbow = addLongbowReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(longbow.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can tap to deal 1 damage to a player")
    void equippedCreatureDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent longbow = addLongbowReady(player1);
        longbow.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("damage from Grizzly Bears"));
    }

    @Test
    @DisplayName("Equipped creature can deal 1 damage to a creature")
    void equippedCreatureDealsDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent longbow = addLongbowReady(player1);
        longbow.setAttachedTo(creature.getId());
        addCreatureReady(player2, new JackalPup());

        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Jackal Pup"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jackal Pup");
    }

    private Permanent addLongbowReady(Player player) {
        Permanent perm = new Permanent(new ViridianLongbow());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
