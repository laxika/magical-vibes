package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.v.VulshokHeartstoker;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrigonOfRage.class, VulshokHeartstoker.class})
class TrigonOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Charging taps immediately but adds the counter only on resolution")
    void chargingUsesTheStackAndRequiresTap() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost removes its last charge counter as a cost and resolves without its source")
    void boostPaysCounterBeforeResolutionAndSurvivesSourceRemoval() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VulshokHeartstoker());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(trigon.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).remove(trigon);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost cannot target a noncreature artifact")
    void boostRejectsNoncreatureTarget() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, trigon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(trigon.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost requires two mana even with a charge counter available")
    void boostRequiresTwoMana() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VulshokHeartstoker());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(trigon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TrigonOfRage()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent trigon = findPermanent(player1, "Trigon of Rage");
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating first ability adds a charge counter")
    void activateFirstAbilityAddsCounter() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        harness.addMana(player1, ManaColor.RED, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("First ability requires red mana")
    void firstAbilityRequiresRedMana() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        // Only colorless mana, should fail
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating second ability gives target creature +3/+0 until end of turn")
    void activateSecondAbilityBoostsCreature() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new VulshokHeartstoker());

        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();

        // Charge counter removed
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        // Target creature gets +3/+0
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("+3/+0 boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new VulshokHeartstoker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();

        // Vulshok Heartstoker is 2/2, +3/+0 = 5/2
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Back to 2/2
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate second ability with 0 charge counters")
    void cannotActivateBoostAbilityWithNoCounters() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 0);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new VulshokHeartstoker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can boost multiple times by untapping between uses")
    void canBoostMultipleTimes() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new VulshokHeartstoker());

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();
        trigon.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        // Vulshok Heartstoker is 2/2, +3/+0 twice = 8/2
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot activate second ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new VulshokHeartstoker());

        // First activation taps it
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfRage());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new VulshokHeartstoker());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
