package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ZealotsEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when all nonland permanents are white")
    void gainsLifeWithOnlyWhiteNonlandPermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new Plains());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger with a nonwhite nonland permanent")
    void doesNotGainLifeWithNonwhiteNonlandPermanent() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does nothing if a nonwhite nonland permanent appears before resolution")
    void doesNothingIfConditionFailsBeforeResolution() {
        harness.addToBattlefield(player1, new ZealotsEnDal());
        harness.addToBattlefield(player1, new SavannahLions());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
