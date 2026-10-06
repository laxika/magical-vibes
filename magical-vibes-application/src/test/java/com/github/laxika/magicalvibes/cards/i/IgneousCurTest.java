package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IgneousCur.class})
class IgneousCurTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+0 until end of turn")
    void resolvingAbilityBoostsPower() {
        Permanent cur = addCreatureReady(player1, new IgneousCur());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cur)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void repeatedActivationsStack() {
        Permanent cur = addCreatureReady(player1, new IgneousCur());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cur)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cur)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent cur = addCreatureReady(player1, new IgneousCur());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cur)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cur)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new IgneousCur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability works while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent cur = harness.addToBattlefieldAndReturn(player1, new IgneousCur());
        cur.setSummoningSick(true);
        cur.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cur)).isEqualTo(2);
        assertThat(cur.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only its source")
    void boostWaitsForResolutionAndAffectsOnlySource() {
        Permanent cur = addCreatureReady(player1, new IgneousCur());
        Permanent other = addCreatureReady(player1, new IgneousCur());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, cur)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cur)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the red mana requirement")
    void cannotActivateWithoutRedMana() {
        Permanent cur = addCreatureReady(player1, new IgneousCur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, cur)).isEqualTo(1);
    }
}