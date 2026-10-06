package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@CardUsed({Sizzle.class, SavannahLions.class, FurnaceOfRath.class})
class SizzleTest extends BaseCardTest {

    @Test
    @DisplayName("Sizzle deals 3 damage to the opponent and none to its controller")
    void dealsThreeToOpponent() {
        castSizzle();
        harness.passBothPriorities(); // resolve the sorcery

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Sizzle deals 3 damage with non-default life totals")
    void dealsThreeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castSizzle();
        harness.passBothPriorities(); // resolve the sorcery

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Sizzle damages the opponent relative to its caster")
    void damagesPlayerOneWhenPlayerTwoCasts() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Sizzle(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sizzle does not damage creatures on either battlefield")
    void leavesCreaturesUnharmed() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player2, new SavannahLions());

        castSizzle();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Savannah Lions");
        harness.assertOnBattlefield(player2, "Savannah Lions");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Furnace of Rath doubles Sizzle's damage to the opponent")
    void damageCanBeDoubled() {
        harness.addToBattlefield(player2, new FurnaceOfRath());

        castSizzle();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    private void castSizzle() {
        harness.castFromHand(player1, new Sizzle(), "{2}{R}");
    }
}
