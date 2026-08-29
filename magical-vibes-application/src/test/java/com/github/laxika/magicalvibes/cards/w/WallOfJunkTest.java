package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfJunkTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking returns Wall of Junk to its owner's hand at end of combat")
    void blockingReturnsItToHand() {
        Permanent attacker = addReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        addReady(player2, new WallOfJunk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Junk");
        harness.assertInHand(player2, "Wall of Junk");
    }

    @Test
    @DisplayName("Wall of Junk that does not block stays on the battlefield")
    void notReturnedWhenItDoesNotBlock() {
        Permanent attacker = addReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        addReady(player2, new WallOfJunk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Junk");
        harness.assertNotInHand(player2, "Wall of Junk");
    }

    @Test
    @DisplayName("Wall of Junk is not returned if it leaves before end of combat")
    void notReturnedIfItLeavesBeforeEndOfCombat() {
        Permanent attacker = addReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        Permanent wall = addReady(player2, new WallOfJunk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(wall);
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Wall of Junk");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
