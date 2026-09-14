package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({WallOfJunk.class, GorillaWarrior.class})
class WallOfJunkTest extends BaseCardTest {

    @Test
    @DisplayName("When Wall of Junk blocks, it returns to its owner's hand at end of combat")
    void blockingReturnsWallToOwnersHandAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfJunk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Wall of Junk");
        harness.assertNotInHand(player2, "Wall of Junk");

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Wall of Junk");
        harness.assertInHand(player2, "Wall of Junk");
        harness.assertOnBattlefield(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Wall of Junk does not return when it never blocks")
    void doesNotReturnWhenItDoesNotBlock() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfJunk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Wall of Junk");
        harness.assertNotInHand(player2, "Wall of Junk");
    }

    @Test
    @DisplayName("A Wall of Junk that leaves the battlefield before end of combat is not returned")
    void doesNotReturnIfItLeavesBeforeEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfJunk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(wall.getId()));

        harness.passBothPriorities();
        harness.assertNotInHand(player2, "Wall of Junk");
    }
}
