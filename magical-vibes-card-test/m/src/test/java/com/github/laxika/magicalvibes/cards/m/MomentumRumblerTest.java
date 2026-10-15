package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArchetypeOfCourage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomentumRumbler.class, ArchetypeOfCourage.class})
class MomentumRumblerTest extends BaseCardTest {

    @Test
    @DisplayName("The first attack puts a first strike counter on Momentum Rumbler")
    void firstAttackPutsFirstStrikeCounterOnIt() {
        Permanent rumbler = addReadyRumbler();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(rumbler.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An attack while it has first strike gives Momentum Rumbler double strike")
    void attackWithFirstStrikeGivesDoubleStrike() {
        Permanent rumbler = addReadyRumbler();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        rumbler.untap();
        rumbler.setAttacking(false);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The intervening first strike condition is checked when the trigger resolves")
    void firstStrikeConditionIsRecheckedOnResolution() {
        Permanent rumbler = addReadyRumbler();

        declareAttackers(player1, List.of(0));
        rumbler.setCounterCount(CounterType.FIRST_STRIKE, 1);
        harness.passBothPriorities();

        assertThat(rumbler.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Losing first strike before resolution prevents the double strike grant")
    void losingFirstStrikeBeforeResolutionPreventsDoubleStrike() {
        Permanent rumbler = addReadyRumbler();
        rumbler.setCounterCount(CounterType.FIRST_STRIKE, 1);

        declareAttackers(player1, List.of(0));
        rumbler.setCounterCount(CounterType.FIRST_STRIKE, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(rumbler.getCounterCount(CounterType.FIRST_STRIKE)).isZero();
    }

    @Test
    @DisplayName("Double strike alone does not prevent gaining a first strike counter")
    void doubleStrikeAloneStillGetsFirstStrikeCounter() {
        Permanent rumbler = addReadyRumbler();
        rumbler.setCounterCount(CounterType.DOUBLE_STRIKE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(rumbler.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(rumbler.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First strike from a static ability qualifies for double strike")
    void staticFirstStrikeQualifiesForDoubleStrike() {
        Permanent rumbler = addReadyRumbler();
        addCreatureReady(player1, new ArchetypeOfCourage());
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.FIRST_STRIKE)).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(rumbler.getCounterCount(CounterType.FIRST_STRIKE)).isZero();
    }

    @Test
    @DisplayName("A suppressed first strike counter does not prevent another first strike counter")
    void suppressedFirstStrikeCounterDoesNotPreventCounterTrigger() {
        Permanent rumbler = addReadyRumbler();
        rumbler.setCounterCount(CounterType.FIRST_STRIKE, 1);
        addCreatureReady(player2, new ArchetypeOfCourage());
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.FIRST_STRIKE)).isFalse();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(rumbler.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, rumbler, Keyword.DOUBLE_STRIKE)).isFalse();
    }
    private Permanent addReadyRumbler() {
        return addCreatureReady(player1, new MomentumRumbler());
    }

}
