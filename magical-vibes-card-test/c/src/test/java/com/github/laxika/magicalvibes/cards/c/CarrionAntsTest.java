package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CarrionAnts.class)
class CarrionAntsTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without paying {1}")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new CarrionAnts());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying {1} gives +1/+1 until end of turn")
    void payManaBoostsSelf() {
        Permanent ants = addCreatureReady(player1, new CarrionAnts());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated multiple times to stack the boost")
    void stacksBoost() {
        Permanent ants = addCreatureReady(player1, new CarrionAnts());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent ants = addCreatureReady(player1, new CarrionAnts());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped, summoning-sick Carrion Ants can pay colored mana to boost itself")
    void activatesWhileTappedAndSummoningSick() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new CarrionAnts());
        ants.setSummoningSick(true);
        ants.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(2);
        assertThat(ants.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activation during an opponent's turn boosts only the source creature")
    void boostsOnlySourceDuringOpponentsTurn() {
        Permanent ants = addCreatureReady(player1, new CarrionAnts());
        Permanent otherAnts = addCreatureReady(player1, new CarrionAnts());
        Permanent opposingAnts = addCreatureReady(player2, new CarrionAnts());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ants)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ants)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherAnts)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, otherAnts)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingAnts)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opposingAnts)).isEqualTo(1);
    }
}
