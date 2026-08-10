package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadIronSledgeTest extends BaseCardTest {

    @Test
    @DisplayName("When the equipped creature blocks, both creatures are destroyed")
    void equippedCreatureBlocks() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent blocker = addReady(player2, new GrizzlyBears());
        Permanent sledge = addReady(player2, new DeadIronSledge());
        sledge.setAttachedTo(blocker.getId());

        declareBlock(attacker, blocker);
        harness.passBothPriorities();

        assertDestroyed(player1, attacker, "Grizzly Bears");
        assertDestroyed(player2, blocker, "Grizzly Bears");
    }

    @Test
    @DisplayName("When the equipped creature becomes blocked, both creatures are destroyed")
    void equippedCreatureBecomesBlocked() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent sledge = addReady(player1, new DeadIronSledge());
        sledge.setAttachedTo(attacker.getId());
        Permanent blocker = addReady(player2, new GrizzlyBears());

        declareBlock(attacker, blocker);
        harness.passBothPriorities();

        assertDestroyed(player1, attacker, "Grizzly Bears");
        assertDestroyed(player2, blocker, "Grizzly Bears");
    }

    @Test
    @DisplayName("Moving the Sledge after the ability triggers does not change the creatures destroyed")
    void movingSledgeAfterTriggerDoesNotChangeCreatures() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent sledge = addReady(player1, new DeadIronSledge());
        sledge.setAttachedTo(attacker.getId());
        Permanent blocker = addReady(player2, new GrizzlyBears());

        declareBlock(attacker, blocker);
        sledge.setAttachedTo(null);
        harness.passBothPriorities();

        assertDestroyed(player1, attacker, "Grizzly Bears");
        assertDestroyed(player2, blocker, "Grizzly Bears");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

    private void assertDestroyed(Player player, Permanent permanent, String cardName) {
        assertThat(gd.playerBattlefields.get(player.getId())).doesNotContain(permanent);
        harness.assertInGraveyard(player, cardName);
    }
}
