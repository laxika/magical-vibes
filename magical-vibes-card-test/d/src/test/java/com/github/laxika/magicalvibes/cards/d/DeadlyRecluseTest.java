package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadlyRecluse.class, AirElemental.class, GiantSpider.class, SafePassage.class})
class DeadlyRecluseTest extends BaseCardTest {

    @Test
    void blocksAndKillsFlyingCreatureDespiteDyingSimultaneously() {
        addCreatureReady(player1, new AirElemental());
        Permanent recluse = addCreatureReady(player2, new DeadlyRecluse());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(recluse.isBlocking()).isTrue();
        resolveCombat();

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Deadly Recluse");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Deadly Recluse");
        harness.assertLife(player2, 20);
    }

    @Test
    void canBlockAndKillNonFlyingCreature() {
        addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player2, new DeadlyRecluse());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Deadly Recluse");
        harness.assertLife(player2, 20);
    }

    @Test
    void oneDamageKillsBlockerWithGreaterToughnessWhenAttacking() {
        addCreatureReady(player1, new DeadlyRecluse());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Deadly Recluse");
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertLife(player2, 20);
    }

    @Test
    void preventedDamageDoesNotDestroyCreatureThroughDeathtouch() {
        addCreatureReady(player1, new DeadlyRecluse());
        addCreatureReady(player2, new GiantSpider());

        harness.castFromHand(player2, new SafePassage(), "{2}{W}");
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Deadly Recluse");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
        harness.assertLife(player2, 20);
    }
}
