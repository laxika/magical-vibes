package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(BorosSwiftblade.class)
class BorosSwiftbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals combat damage twice")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new BorosSwiftblade());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
