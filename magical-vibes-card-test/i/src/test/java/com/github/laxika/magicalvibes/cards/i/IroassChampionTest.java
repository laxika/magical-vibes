package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.v.VastwoodGorger;
import com.github.laxika.magicalvibes.cards.y.YevasForcemage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IroassChampion.class, VastwoodGorger.class, YevasForcemage.class})
class IroassChampionTest extends BaseCardTest {

    @Test
    void unblockedChampionDealsDamageInBothSteps() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new IroassChampion());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player1, "Iroas's Champion");
    }

    @Test
    void killingBlockerInFirstStrikeDoesNotDealDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new IroassChampion());
        harness.addToBattlefield(player2, new YevasForcemage());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Iroas's Champion");
        harness.assertInGraveyard(player2, "Yeva's Forcemage");
        harness.assertLife(player2, 20);
    }

    @Test
    void survivingBlockerTakesBothHitsAndDealsRegularDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new IroassChampion());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new VastwoodGorger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Vastwood Gorger");
        harness.assertInGraveyard(player1, "Iroas's Champion");
        harness.assertLife(player2, 20);
    }

    @Test
    void blockingChampionKillsAttackerBeforeRegularDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new YevasForcemage());
        harness.addToBattlefield(player2, new IroassChampion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Yeva's Forcemage");
        harness.assertOnBattlefield(player2, "Iroas's Champion");
        harness.assertLife(player2, 20);
    }

    @Test
    void opposingChampionsDealLethalFirstStrikeDamageSimultaneously() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new IroassChampion());
        harness.addToBattlefield(player2, new IroassChampion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Iroas's Champion");
        harness.assertInGraveyard(player2, "Iroas's Champion");
        harness.assertLife(player2, 20);
    }
}
