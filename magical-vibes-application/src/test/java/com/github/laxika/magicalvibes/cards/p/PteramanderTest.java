package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pteramander.class, Shock.class, Divination.class, Forest.class})
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

    @Test
    void excessGraveyardCardsReduceCostToOneBlueMana() {
        Permanent pteramander = harness.addToBattlefieldAndReturn(player1, new Pteramander());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Divination(), new Divination(), new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pteramander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void reductionCannotPayTheBlueManaRequirement() {
        harness.addToBattlefield(player1, new Pteramander());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Divination(), new Divination(), new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsGraveyardDoesNotReduceCost() {
        harness.addToBattlefield(player1, new Pteramander());
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterAddedBeforeResolutionPreventsAdapt() {
        Permanent pteramander = harness.addToBattlefieldAndReturn(player1, new Pteramander());
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.activateAbility(player1, 0, null, null);

        pteramander.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(pteramander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterRemovedBeforeResolutionAllowsAdapt() {
        Permanent pteramander = harness.addToBattlefieldAndReturn(player1, new Pteramander());
        pteramander.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.activateAbility(player1, 0, null, null);

        pteramander.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(pteramander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void canAdaptAgainAfterLosingAllCounters() {
        Permanent pteramander = harness.addToBattlefieldAndReturn(player1, new Pteramander());
        harness.addMana(player1, ManaColor.BLUE, 16);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(pteramander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        pteramander.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pteramander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
