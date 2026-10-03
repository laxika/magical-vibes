package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GuardianLions;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AjanisSunstriker.class, GuardianLions.class})
class AjanisSunstrikerTest extends BaseCardTest {

    @Test
    void unblockedDamageGainsLifeForAttackingController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AjanisSunstriker());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void damageToCreatureGainsLifeWithoutDamagingDefendingPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AjanisSunstriker());
        addCreatureReady(player2, new GuardianLions());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Ajani's Sunstriker");
        harness.assertOnBattlefield(player2, "Guardian Lions");
    }

    @Test
    void bothControllersGainLifeEvenWhenBothSunstrikersDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AjanisSunstriker());
        addCreatureReady(player2, new AjanisSunstriker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player1, "Ajani's Sunstriker");
        harness.assertInGraveyard(player2, "Ajani's Sunstriker");
    }
}
