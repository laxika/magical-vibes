package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.u.UnboundedPotential;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightedMyr.class, UnboundedPotential.class})
class KnightedMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Adapt puts a +1/+1 counter on Knighted Myr and grants double strike")
    void adaptAddsCounterAndGrantsDoubleStrike() {
        Permanent myr = addMyr();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent myr = addMyr();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Adapt does not add a counter or trigger double strike when one is already present")
    void adaptDoesNotTriggerWithExistingCounter() {
        Permanent myr = addMyr();
        myr.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Counters placed by an opponent trigger double strike even with existing counters")
    void opponentCounterPlacementTriggersDoubleStrike() {
        Permanent myr = addMyr();
        Permanent otherMyr = addCreatureReady(player1, new KnightedMyr());
        myr.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new UnboundedPotential()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(myr.getId()));
        harness.passBothPriorities();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherMyr, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Two adapt activations only add one counter when both resolve")
    void adaptChecksCountersAtResolution() {
        Permanent myr = addMyr();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myr, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    private Permanent addMyr() {
        return addCreatureReady(player1, new KnightedMyr());
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
