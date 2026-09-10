package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ArabellaAbandonedDoll.class, GrizzlyBears.class, HillGiant.class})
class ArabellaAbandonedDollTest extends BaseCardTest {

    @Test
    void attackingDealsDamageAndGainsLifeForSmallCreaturesYouControl() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ArabellaAbandonedDoll());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 17);
    }
}
