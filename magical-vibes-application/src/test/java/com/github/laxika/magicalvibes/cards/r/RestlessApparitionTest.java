package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RestlessApparition.class})
class RestlessApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +3/+3, payable with white mana")
    void resolvingBoostsWithWhite() {
        Permanent apparition = addCreatureReady(player1, new RestlessApparition());
        int basePower = apparition.getEffectivePower();
        int baseToughness = apparition.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower + 3);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Ability is also payable with black mana (hybrid cost)")
    void payableWithBlack() {
        Permanent apparition = addCreatureReady(player1, new RestlessApparition());
        int basePower = apparition.getEffectivePower();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower + 3);
    }

    @Test
    @DisplayName("Can activate multiple times, stacking the boost")
    void stacksMultipleActivations() {
        Permanent apparition = addCreatureReady(player1, new RestlessApparition());
        int basePower = apparition.getEffectivePower();
        int baseToughness = apparition.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower + 6);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(baseToughness + 6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent apparition = addCreatureReady(player1, new RestlessApparition());
        int basePower = apparition.getEffectivePower();
        int baseToughness = apparition.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower + 3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new RestlessApparition());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void mixedHybridManaPaysForAbilityWhileTappedAndSummoningSick() {
        Permanent apparition = harness.addToBattlefieldAndReturn(player1, new RestlessApparition());
        apparition.setSummoningSick(true);
        apparition.tap();
        int power = apparition.getEffectivePower();
        int toughness = apparition.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(power + 3);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(toughness + 3);
        assertThat(apparition.isTapped()).isTrue();
    }

    @Test
    void unrelatedColoredManaCannotPayHybridCost() {
        harness.addToBattlefield(player1, new RestlessApparition());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void persistReturnsWithCounterButDoesNotReturnAfterSecondDeath() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new RestlessApparition());
        original.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Restless Apparition");
        harness.assertNotOnBattlefield(player1, "Restless Apparition");

        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Restless Apparition");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Restless Apparition");

        returned.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Restless Apparition");
        harness.assertInGraveyard(player1, "Restless Apparition");
    }

    @Test
    void pendingPumpDoesNotBoostNewPermanentAfterPersist() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new RestlessApparition());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);
        original.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Restless Apparition");
        int power = returned.getEffectivePower();
        int toughness = returned.getEffectiveToughness();
        resolveAllTriggers();

        assertThat(returned.getEffectivePower()).isEqualTo(power);
        assertThat(returned.getEffectiveToughness()).isEqualTo(toughness);
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
