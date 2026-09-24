package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalVigor.class, BladeSplicer.class, Pentavus.class})
class PrimalVigorTest extends BaseCardTest {

    @Test
    void doublesTokensCreatedByAnyPlayer() {
        harness.addToBattlefield(player1, new PrimalVigor());
        harness.enterBattlefieldAndReturn(player2, new BladeSplicer());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Golem")).hasSize(2);
    }

    @Test
    void doublesPlusOnePlusOneCountersOnAnyCreature() {
        harness.addToBattlefield(player1, new PrimalVigor());
        Permanent pentavus = harness.enterBattlefieldAndReturn(player2, new Pentavus());

        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }
}
