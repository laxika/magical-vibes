package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BeastsOfBogardan;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({RamirezDePietro.class, BeastsOfBogardan.class})
class RamirezDePietroTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 3/3 blocker before it deals combat damage")
    void firstStrikeKillsBlockerBeforeItDealsCombatDamage() {
        Permanent ramirez = addCreatureReady(player1, new RamirezDePietro());
        ramirez.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BeastsOfBogardan());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Ramirez DePietro");
        harness.assertInGraveyard(player2, "Beasts of Bogardan");
    }

    @Test
    @DisplayName("First strike kills an attacker before it damages Ramirez")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new BeastsOfBogardan());
        attacker.setAttacking(true);

        Permanent ramirez = addCreatureReady(player2, new RamirezDePietro());
        ramirez.setBlocking(true);
        ramirez.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Beasts of Bogardan");
        harness.assertOnBattlefield(player2, "Ramirez DePietro");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked first striker deals damage only once")
    void unblockedFirstStrikerDoesNotDealRegularCombatDamage() {
        Permanent ramirez = addCreatureReady(player1, new RamirezDePietro());
        ramirez.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player1, "Ramirez DePietro");
    }
}
