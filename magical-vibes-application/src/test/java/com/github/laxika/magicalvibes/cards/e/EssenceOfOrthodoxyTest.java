package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianBroodlings;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceOfOrthodoxy.class, PhyrexianBroodlings.class, GrizzlyBears.class})
class EssenceOfOrthodoxyTest extends BaseCardTest {

    @Test
    void incubatesWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new EssenceOfOrthodoxy());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).singleElement()
                .satisfies(incubator -> assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }

    @Test
    void incubatesWhenAnotherPhyrexianEnters() {
        harness.enterBattlefieldAndReturn(player1, new EssenceOfOrthodoxy());
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new PhyrexianBroodlings());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).hasSize(2)
                .allSatisfy(incubator -> assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }

    @Test
    void doesNotIncubateWhenANonPhyrexianEnters() {
        harness.enterBattlefieldAndReturn(player1, new EssenceOfOrthodoxy());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(findPermanents(player1, "Incubator")).hasSize(1);
    }

    @Test
    void doesNotTriggerForAnOpponentsPhyrexian() {
        harness.enterBattlefieldAndReturn(player1, new EssenceOfOrthodoxy());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new PhyrexianBroodlings());

        assertThat(findPermanents(player1, "Incubator")).hasSize(1);
    }
}
