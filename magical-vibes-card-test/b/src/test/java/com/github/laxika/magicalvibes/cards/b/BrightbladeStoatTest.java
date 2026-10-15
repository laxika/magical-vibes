package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({BrightbladeStoat.class, GrizzlyBears.class})
class BrightbladeStoatTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills an equal-toughness blocker and lifelink gains life")
    void firstStrikeAndLifelinkWorkInCombat() {
        harness.setLife(player1, 20);

        Permanent attacker = addCreatureReady(player1, new BrightbladeStoat());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Brightblade Stoat");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @CardUsed(BrightbladeStoat.class)
    @DisplayName("An unblocked Stoat deals damage and gains life only once")
    void unblockedStoatDealsDamageOnlyOnce() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new BrightbladeStoat());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed(BrightbladeStoat.class)
    @DisplayName("Opposing Stoats deal first-strike damage simultaneously and both gain life")
    void opposingStoatsBothDieAndGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new BrightbladeStoat());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BrightbladeStoat());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Brightblade Stoat");
        harness.assertNotOnBattlefield(player2, "Brightblade Stoat");
        harness.assertInGraveyard(player1, "Brightblade Stoat");
        harness.assertInGraveyard(player2, "Brightblade Stoat");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
    }
}
