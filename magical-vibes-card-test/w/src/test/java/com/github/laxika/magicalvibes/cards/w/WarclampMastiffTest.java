package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.s.SilvercoatLion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({WarclampMastiff.class, MerfolkOfThePearlTrident.class, SilvercoatLion.class})
class WarclampMastiffTest extends BaseCardTest {

    @Test
    void attackingMastiffKillsOrdinaryOneOneBeforeItCanDealDamage() {
        Permanent attacker = addCreatureReady(player1, new WarclampMastiff());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MerfolkOfThePearlTrident());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Warclamp Mastiff");
        harness.assertInGraveyard(player2, "Merfolk of the Pearl Trident");
        harness.assertLife(player2, 20);
    }

    @Test
    void blockingMastiffKillsOrdinaryOneOneBeforeItCanDealDamage() {
        Permanent attacker = addCreatureReady(player2, new MerfolkOfThePearlTrident());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new WarclampMastiff());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Warclamp Mastiff");
        harness.assertInGraveyard(player2, "Merfolk of the Pearl Trident");
        harness.assertLife(player1, 20);
    }

    @Test
    void survivingBlockerDealsRegularDamageAndKillsMastiff() {
        Permanent attacker = addCreatureReady(player1, new WarclampMastiff());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SilvercoatLion());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Warclamp Mastiff");
        harness.assertOnBattlefield(player2, "Silvercoat Lion");
        harness.assertLife(player2, 20);
    }

    @Test
    void opposingMastiffsDealFirstStrikeDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new WarclampMastiff());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WarclampMastiff());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Warclamp Mastiff");
        harness.assertInGraveyard(player2, "Warclamp Mastiff");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedMastiffDealsDamageOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new WarclampMastiff());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Warclamp Mastiff");
    }
}
