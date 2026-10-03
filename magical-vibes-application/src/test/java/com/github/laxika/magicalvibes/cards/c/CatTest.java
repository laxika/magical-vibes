package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({Cat.class})
class CatTest extends BaseCardTest {

    @Test
    void combatDamageToPlayerGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cat = addCreatureReady(player1, new Cat());
        cat.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void bothCatsGainLifeFromCreatureDamageEvenWhenTheyDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new Cat());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Cat());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertNotOnBattlefield(player1, "Cat");
        harness.assertNotOnBattlefield(player2, "Cat");
    }
}
