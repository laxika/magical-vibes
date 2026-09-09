package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WelkinHawk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ZealotsEnDal.class, WelkinHawk.class, RagingGoblin.class, CityOfTraitors.class})
class ZealotsEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when all nonland permanents are white")
    void gainsLifeWithOnlyWhiteNonlandPermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new WelkinHawk());
        harness.addToBattlefield(player1, new CityOfTraitors());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger with a nonwhite nonland permanent")
    void doesNotGainLifeWithNonwhiteNonlandPermanent() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new RagingGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does nothing if a nonwhite nonland permanent appears before resolution")
    void doesNothingIfConditionFailsBeforeResolution() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new WelkinHawk());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Ignores nonwhite permanents controlled by an opponent")
    void ignoresOpponentsNonwhitePermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new WelkinHawk());
        harness.addToBattlefield(player2, new RagingGoblin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }
}
