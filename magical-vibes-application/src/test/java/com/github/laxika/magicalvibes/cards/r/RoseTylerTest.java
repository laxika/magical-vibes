package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoseTyler.class, GrizzlyBears.class, AncestralVision.class})
class RoseTylerTest extends BaseCardTest {

    @Test
    void getsPlusOnePlusOneForEachTimeCounterOnIt() {
        Permanent rose = addCreatureReady(player1, new RoseTyler());
        rose.setCounterCount(CounterType.TIME, 2);

        assertThat(gqs.getEffectivePower(gd, rose)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rose)).isEqualTo(4);
    }

    @Test
    void attackPutsCountersForOwnedSuspendedCardsAndOtherCounteredPermanents() {
        Permanent rose = addCreatureReady(player1, new RoseTyler());
        Permanent otherPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherPermanent.setCounterCount(CounterType.TIME, 1);

        AncestralVision suspendedCard = new AncestralVision();
        harness.setExile(player1, List.of(suspendedCard));
        gd.exiledCardTimeCounters.put(suspendedCard.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(rose.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }
}
