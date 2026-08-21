package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PteramanderTest extends BaseCardTest {

    @Test
    void adaptPutsCountersOnCreatureWithoutPlusOneCounters() {
        Permanent pteramander = harness.addToBattlefieldAndReturn(player1, new Pteramander());
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pteramander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, pteramander)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, pteramander)).isEqualTo(5);
    }

    @Test
    void adaptDoesNothingWhenCreatureAlreadyHasPlusOneCounter() {
        Permanent pteramander = harness.addToBattlefieldAndReturn(player1, new Pteramander());
        pteramander.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pteramander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void instantAndSorceryCardsReduceAdaptCostButOtherCardsDoNot() {
        Permanent pteramander = harness.addToBattlefieldAndReturn(player1, new Pteramander());
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pteramander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
