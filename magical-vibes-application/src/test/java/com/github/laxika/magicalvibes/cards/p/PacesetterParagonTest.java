package com.github.laxika.magicalvibes.cards.p;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pacesetter Paragon")
@CardUsed({PacesetterParagon.class})
class PacesetterParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts a +1/+1 counter on it and gives it double strike")
    void exhaustAbility() {
        Permanent paragon = addParagon();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Double strike wears off at end of turn but the counter remains")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent paragon = addParagon();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each exhaust ability can be activated only once")
    void cannotExhaustTwice() {
        addParagon();
        addExhaustMana();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Exhaust is consumed on activation before its effects resolve")
    void cannotExhaustAgainWhileAbilityIsOnStack() {
        Permanent paragon = addParagon();
        addExhaustMana();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness can exhaust")
    void canExhaustWhileTappedAndSummoningSick() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new PacesetterParagon());
        paragon.setSummoningSick(true);
        paragon.setTapped(true);
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each copy has its own exhaust activation and affects only itself")
    void exhaustLimitsAreIndependentForEachPermanent() {
        Permanent first = addParagon();
        Permanent second = addParagon();
        addExhaustMana();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private Permanent addParagon() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new PacesetterParagon());
        paragon.setSummoningSick(false);
        return paragon;
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
