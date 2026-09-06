package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({LandLeeches.class, GrizzlyBears.class, HillGiant.class})
class LandLeechesTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 2/2 blocker before it deals regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new LandLeeches());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Land Leeches deals 2 first strike damage, killing the 2/2 before it can deal damage back.
        harness.assertOnBattlefield(player1, "Land Leeches");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Land Leeches still dies if the blocker survives first strike damage")
    void diesIfBlockerSurvivesFirstStrike() {
        Permanent attacker = addCreatureReady(player1, new LandLeeches());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // 2 first strike damage doesn't kill the 3/3; it deals 3 back and the 2/2 dies.
        harness.assertNotOnBattlefield(player1, "Land Leeches");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }
}
