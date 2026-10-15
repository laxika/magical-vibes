package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PrimalHuntbeast;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({GiantScorpion.class, PrimalHuntbeast.class, SafePassage.class})
class GiantScorpionTest extends BaseCardTest {

    @Test
    void attackingScorpionKillsHexproofBlockerDespiteDyingSimultaneously() {
        addCreatureReady(player1, new GiantScorpion());
        addCreatureReady(player2, new PrimalHuntbeast());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Giant Scorpion");
        harness.assertInGraveyard(player2, "Primal Huntbeast");
        harness.assertNotOnBattlefield(player1, "Giant Scorpion");
        harness.assertNotOnBattlefield(player2, "Primal Huntbeast");
        harness.assertLife(player2, 20);
    }

    @Test
    void blockingScorpionKillsAttackerDespiteDyingSimultaneously() {
        addCreatureReady(player1, new PrimalHuntbeast());
        addCreatureReady(player2, new GiantScorpion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Primal Huntbeast");
        harness.assertInGraveyard(player2, "Giant Scorpion");
        harness.assertNotOnBattlefield(player1, "Primal Huntbeast");
        harness.assertNotOnBattlefield(player2, "Giant Scorpion");
        harness.assertLife(player2, 20);
    }

    @Test
    void preventedDamageDoesNotDestroyBlockerThroughDeathtouch() {
        addCreatureReady(player1, new GiantScorpion());
        addCreatureReady(player2, new PrimalHuntbeast());

        harness.castFromHand(player2, new SafePassage(), "{2}{W}");
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Giant Scorpion");
        harness.assertOnBattlefield(player2, "Primal Huntbeast");
        harness.assertNotInGraveyard(player2, "Primal Huntbeast");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedScorpionDealsNormalDamageToPlayer() {
        addCreatureReady(player1, new GiantScorpion());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Giant Scorpion");
    }
}
