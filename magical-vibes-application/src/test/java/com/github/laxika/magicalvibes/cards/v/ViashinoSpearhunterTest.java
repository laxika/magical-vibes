package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({ViashinoSpearhunter.class, RuneclawBear.class})
class ViashinoSpearhunterTest extends BaseCardTest {

    @Test
    void killsBlockerBeforeItCanDealDamage() {
        Permanent attacker = addCreatureReady(player1, new ViashinoSpearhunter());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Viashino Spearhunter");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertLife(player2, 20);
    }

    @Test
    void killsAttackerBeforeItCanDealDamageWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ViashinoSpearhunter());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Viashino Spearhunter");
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsDamageOnlyOnceWhenUnblocked() {
        Permanent attacker = addCreatureReady(player1, new ViashinoSpearhunter());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Viashino Spearhunter");
    }

    @Test
    void bothFirstStrikersDealDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new ViashinoSpearhunter());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ViashinoSpearhunter());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Viashino Spearhunter");
        harness.assertNotOnBattlefield(player2, "Viashino Spearhunter");
        harness.assertInGraveyard(player1, "Viashino Spearhunter");
        harness.assertInGraveyard(player2, "Viashino Spearhunter");
        harness.assertLife(player2, 20);
    }
}
