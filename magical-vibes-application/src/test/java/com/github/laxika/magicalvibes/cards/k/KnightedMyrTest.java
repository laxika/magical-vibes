package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KnightedMyr.class)
class KnightedMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Adapt puts a +1/+1 counter on Knighted Myr and grants double strike")
    void adaptPutsCounterAndGrantsDoubleStrike() {
        Permanent myr = addKnightedMyr();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike granted by Knighted Myr wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent myr = addKnightedMyr();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Adapt does not add another counter when Knighted Myr already has one")
    void adaptDoesNotAddCounterWhenAlreadyCountered() {
        Permanent myr = addKnightedMyr();
        myr.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addKnightedMyr() {
        return harness.addToBattlefieldAndReturn(player1, new KnightedMyr());
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
