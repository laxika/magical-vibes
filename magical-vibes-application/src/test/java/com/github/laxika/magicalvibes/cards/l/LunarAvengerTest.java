package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LunarAvenger.class)
class LunarAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Sunburst puts one +1/+1 counter on Lunar Avenger for each color spent")
    void sunburstCountsDistinctColors() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent avenger = castAndResolve();

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter grants a chosen keyword until end of turn")
    void removesCounterAndGrantsChosenKeyword() {
        harness.addMana(player1, ManaColor.GREEN, 7);

        Permanent avenger = castAndResolve();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "HASTE");

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avenger, Keyword.HASTE)).isFalse();
    }

    @ParameterizedTest(name = "Choosing {0} grants that keyword")
    @ValueSource(strings = {"FLYING", "FIRST_STRIKE"})
    @DisplayName("Removing a counter grants each untested keyword choice")
    void grantsFlyingOrFirstStrike(String choice) {
        harness.addMana(player1, ManaColor.GREEN, 7);

        Permanent avenger = castAndResolve();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, choice);

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.valueOf(choice))).isTrue();
    }

    @Test
    @DisplayName("The keyword ability cannot be activated without a +1/+1 counter")
    void cannotActivateWithoutPlusOnePlusOneCounter() {
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        Permanent avenger = castAndResolve();

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent castAndResolve() {
        harness.setHand(player1, List.of(new LunarAvenger()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        return findPermanent(player1, "Lunar Avenger");
    }
}
