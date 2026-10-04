package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({FeralAbomination.class, PrimordialWurm.class})
class FeralAbominationTest extends BaseCardTest {

    @Test
    void deathtouchDestroysBlockerWithMoreToughnessThanItsPower() {
        addCreatureReady(player1, new FeralAbomination());
        addCreatureReady(player2, new PrimordialWurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Feral Abomination");
        harness.assertInGraveyard(player2, "Primordial Wurm");
        harness.assertNotOnBattlefield(player1, "Feral Abomination");
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        harness.assertLife(player2, 20);
    }

    @Test
    void deathtouchAlsoDestroysLargerAttackerWhenBlocking() {
        addCreatureReady(player1, new PrimordialWurm());
        addCreatureReady(player2, new FeralAbomination());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Primordial Wurm");
        harness.assertInGraveyard(player2, "Feral Abomination");
        harness.assertNotOnBattlefield(player1, "Primordial Wurm");
        harness.assertNotOnBattlefield(player2, "Feral Abomination");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedAttackDealsNormalDamageToPlayer() {
        addCreatureReady(player1, new FeralAbomination());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Feral Abomination");
    }
}
