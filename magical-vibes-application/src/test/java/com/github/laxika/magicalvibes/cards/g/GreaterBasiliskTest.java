package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({GreaterBasilisk.class, GiantSpider.class, SafePassage.class})
class GreaterBasiliskTest extends BaseCardTest {

    @Test
    void deathtouchDestroysBlockerWithMoreToughnessThanDamageDealt() {
        addCreatureReady(player1, new GreaterBasilisk());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player1, "Greater Basilisk");
        harness.assertLife(player2, 20);
    }

    @Test
    void deathtouchAlsoDestroysAttackerWhenBlocking() {
        addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player2, new GreaterBasilisk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player2, "Greater Basilisk");
        harness.assertLife(player2, 20);
    }

    @Test
    void fullyPreventedDamageDoesNotDestroyBlocker() {
        addCreatureReady(player1, new GreaterBasilisk());
        addCreatureReady(player2, new GiantSpider());

        harness.castFromHand(player2, new SafePassage(), "{2}{W}");
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Greater Basilisk");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedDeathtouchCreatureDealsNormalDamageToPlayer() {
        addCreatureReady(player1, new GreaterBasilisk());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Greater Basilisk");
    }
}
