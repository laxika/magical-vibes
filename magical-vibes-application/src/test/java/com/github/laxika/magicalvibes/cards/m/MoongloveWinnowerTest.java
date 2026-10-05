package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.OakgnarlWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({MoongloveWinnower.class, OakgnarlWarrior.class})
class MoongloveWinnowerTest extends BaseCardTest {

    @Test
    void deathtouchDestroysBlockerDespiteItsGreaterToughness() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MoongloveWinnower());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OakgnarlWarrior());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Moonglove Winnower");
        harness.assertInGraveyard(player2, "Oakgnarl Warrior");
        harness.assertNotOnBattlefield(player2, "Oakgnarl Warrior");
        harness.assertLife(player2, 20);
    }

    @Test
    void deathtouchAlsoDestroysAttackerWhenWinnowerBlocks() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OakgnarlWarrior());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new MoongloveWinnower());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Oakgnarl Warrior");
        harness.assertInGraveyard(player2, "Moonglove Winnower");
        harness.assertNotOnBattlefield(player1, "Oakgnarl Warrior");
    }

    @Test
    void unblockedDamageOnlyReducesPlayersLifeByItsPower() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MoongloveWinnower());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Moonglove Winnower");
    }
}
