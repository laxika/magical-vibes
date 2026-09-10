package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({PeachGardenOath.class, ShuFootSoldiers.class, Forest.class})
class PeachGardenOathTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each creature you control")
    void gainsTwoLifePerControlledCreature() {
        harness.addToBattlefield(player1, new ShuFootSoldiers());
        harness.addToBattlefield(player1, new ShuFootSoldiers());
        harness.addToBattlefield(player1, new ShuFootSoldiers());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Does not count opponent's creatures")
    void doesNotCountOpponentCreatures() {
        harness.addToBattlefield(player2, new ShuFootSoldiers());
        harness.addToBattlefield(player2, new ShuFootSoldiers());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count non-creature permanents")
    void doesNotCountNonCreaturePermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Gains no life when controlling no creatures")
    void gainsNoLifeWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts creatures when the spell resolves")
    void countsCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.addToBattlefield(player1, new ShuFootSoldiers());

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }
}
