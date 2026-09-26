package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadIronSledge.class, CopperMyr.class})
class DeadIronSledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {2} attaches Dead-Iron Sledge to a target creature")
    void equipAttachesToCreature() {
        Permanent sledge = harness.addToBattlefieldAndReturn(player1, new DeadIronSledge());
        Permanent creature = addCreatureReady(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sledge.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("When the equipped creature blocks, both creatures are destroyed")
    void equippedCreatureBlocks() {
        Permanent attacker = addCreatureReady(player1, new CopperMyr());
        Permanent blocker = addCreatureReady(player2, new CopperMyr());
        Permanent sledge = harness.addToBattlefieldAndReturn(player2, new DeadIronSledge());
        sledge.setAttachedTo(blocker.getId());

        declareBlock(attacker, blocker);
        harness.passBothPriorities();

        assertDestroyed(player1, attacker, "Copper Myr");
        assertDestroyed(player2, blocker, "Copper Myr");
    }

    @Test
    @DisplayName("When the equipped creature becomes blocked, both creatures are destroyed")
    void equippedCreatureBecomesBlocked() {
        Permanent attacker = addCreatureReady(player1, new CopperMyr());
        Permanent sledge = harness.addToBattlefieldAndReturn(player1, new DeadIronSledge());
        sledge.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new CopperMyr());

        declareBlock(attacker, blocker);
        harness.passBothPriorities();

        assertDestroyed(player1, attacker, "Copper Myr");
        assertDestroyed(player2, blocker, "Copper Myr");
    }

    @Test
    @DisplayName("Moving the Sledge after the ability triggers does not change the creatures destroyed")
    void movingSledgeAfterTriggerDoesNotChangeCreatures() {
        Permanent attacker = addCreatureReady(player1, new CopperMyr());
        Permanent sledge = harness.addToBattlefieldAndReturn(player1, new DeadIronSledge());
        sledge.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new CopperMyr());

        declareBlock(attacker, blocker);
        sledge.setAttachedTo(null);
        harness.passBothPriorities();

        assertDestroyed(player1, attacker, "Copper Myr");
        assertDestroyed(player2, blocker, "Copper Myr");
    }

    @Test
    @DisplayName("When the equipped creature becomes blocked by two creatures, all three are destroyed")
    void equippedCreatureBecomesBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new CopperMyr());
        Permanent sledge = harness.addToBattlefieldAndReturn(player1, new DeadIronSledge());
        sledge.setAttachedTo(attacker.getId());
        Permanent firstBlocker = addCreatureReady(player2, new CopperMyr());
        Permanent secondBlocker = addCreatureReady(player2, new CopperMyr());

        declareBlocks(attacker, List.of(firstBlocker, secondBlocker));
        resolveAllTriggers();

        assertDestroyed(player1, attacker, "Copper Myr");
        assertDestroyed(player2, firstBlocker, "Copper Myr");
        assertDestroyed(player2, secondBlocker, "Copper Myr");
    }

    @Test
    @DisplayName("An unequipped Sledge does not trigger when a creature blocks")
    void unequippedSledgeDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new CopperMyr());
        Permanent blocker = addCreatureReady(player2, new CopperMyr());
        harness.addToBattlefieldAndReturn(player2, new DeadIronSledge());

        declareBlock(attacker, blocker);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Dead-Iron Sledge"));
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        declareBlocks(attacker, List.of(blocker));
    }

    private void declareBlocks(Permanent attacker, List<Permanent> blockers) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        List<BlockerAssignment> assignments = blockers.stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex))
                .toList();
        gs.declareBlockers(gd, player2, assignments);
    }

    private void assertDestroyed(Player player, Permanent permanent, String cardName) {
        assertThat(gd.playerBattlefields.get(player.getId())).doesNotContain(permanent);
        harness.assertInGraveyard(player, cardName);
    }
}
