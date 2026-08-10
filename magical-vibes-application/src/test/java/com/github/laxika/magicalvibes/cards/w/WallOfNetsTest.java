package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfNetsTest extends BaseCardTest {

    @Test
    @DisplayName("At end of combat, Wall of Nets exiles the creature it blocked")
    void exilesBlockedCreatureAtEndOfCombat() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Giant Spider"));
        harness.assertOnBattlefield(player2, "Wall of Nets");
    }

    @Test
    @DisplayName("A creature not blocked by Wall of Nets is not exiled")
    void doesNotExileUnblockedCreature() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Creatures exiled by Wall of Nets return under their owners' control when it leaves")
    void returnsExiledCreatureWhenWallLeaves() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent wall = addReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Giant Spider");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wall));

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Wall of Nets leaving before end of combat prevents its exile trigger")
    void leavingBeforeEndOfCombatPreventsExile() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent wall = addReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wall));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Giant Spider"));
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
