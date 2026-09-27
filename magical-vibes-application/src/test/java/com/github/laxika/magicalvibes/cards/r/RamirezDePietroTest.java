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
}
