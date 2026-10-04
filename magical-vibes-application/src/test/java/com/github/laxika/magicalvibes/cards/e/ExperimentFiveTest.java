package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.ManaConfluence;
import com.github.laxika.magicalvibes.cards.v.VividCreek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExperimentFive.class, ManaConfluence.class, VividCreek.class})
class ExperimentFiveTest extends BaseCardTest {

    @Test
    @DisplayName("{Z} can be paid with mana from a multicolored source")
    void multicoloredSourceManaPaysZCost() {
        Permanent confluence = harness.addToBattlefieldAndReturn(player1, new ManaConfluence());
        Permanent experiment = addCreatureReady(player1, new ExperimentFive());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(confluence.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, experiment)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, experiment)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("{Z} cannot be paid with ordinary mana")
    void ordinaryManaCannotPayZCost() {
        addCreatureReady(player1, new ExperimentFive());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void qualifyingManaStillRequiresAnotherManaForGenericCost() {
        harness.addToBattlefield(player1, new ManaConfluence());
        Permanent experiment = harness.addToBattlefieldAndReturn(player1, new ExperimentFive());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isZero();
    }

    @Test
    void tappedSummoningSickCreatureCanActivateAndCounterWaitsForResolution() {
        harness.addToBattlefield(player1, new ManaConfluence());
        Permanent experiment = harness.addToBattlefieldAndReturn(player1, new ExperimentFive());
        experiment.setSummoningSick(true);
        experiment.setTapped(true);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isEqualTo(1);
        assertThat(experiment.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, experiment)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, experiment)).isEqualTo(3);
    }

    @Test
    void repeatedActivationsStackCountersAndSpendDistinctQualifyingMana() {
        harness.addToBattlefield(player1, new ManaConfluence());
        harness.addToBattlefield(player1, new ManaConfluence());
        Permanent experiment = harness.addToBattlefieldAndReturn(player1, new ExperimentFive());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, experiment)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, experiment)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void fixedColorAbilityOfMulticoloredCapableSourcePaysZCost() {
        Permanent creek = harness.addToBattlefieldAndReturn(player1, new VividCreek());
        creek.setCounterCount(CounterType.CHARGE, 2);
        Permanent experiment = harness.addToBattlefieldAndReturn(player1, new ExperimentFive());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creek.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
