package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianBroodlings;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TangledSkyline.class, PhyrexianBroodlings.class, GrizzlyBears.class})
class TangledSkylineTest extends BaseCardTest {

    @Test
    void entersWithFiveLifeAndAnIncubatorWithFiveCounters() {
        harness.setHand(player1, List.of(new TangledSkyline()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(findPermanent(player1, "Incubator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void givesReachToPhyrexiansOnlyUnderItsControllersControl() {
        harness.addToBattlefield(player1, new TangledSkyline());
        Permanent ownPhyrexian = harness.addToBattlefieldAndReturn(player1, new PhyrexianBroodlings());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingPhyrexian = harness.addToBattlefieldAndReturn(player2, new PhyrexianBroodlings());

        assertThat(gqs.hasKeyword(gd, ownPhyrexian, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingPhyrexian, Keyword.REACH)).isFalse();
    }
}
