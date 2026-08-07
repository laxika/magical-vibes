package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class HeatStrokeTest extends BaseCardTest {

    @Test
    @DisplayName("At end of combat both the blocked attacker and its blocker are destroyed")
    void destroysBlockerAndBlockedAttacker() {
        Permanent attacker = addReadySpider(player1); // 2/4, survives the blocker's 2 damage
        attacker.setAttacking(true);
        addReadySpider(player2);
        addHeatStroke(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Combat damage, then the end-of-combat trigger goes on the stack and resolves.
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("A creature that stayed out of the block is untouched")
    void sparesCreaturesThatDidNotBlock() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        addReadySpider(player2);
        addReadyBystander(player2);
        addHeatStroke(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With no blocks declared, the unblocked attacker survives end of combat")
    void sparesUnblockedAttacker() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        addHeatStroke(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Spider");
    }

    private void addHeatStroke(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new HeatStroke()));
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBystander(Player player) {
        Permanent perm = new Permanent(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
