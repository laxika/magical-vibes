package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HematiteGolem.class)
class HematiteGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+0 until end of turn")
    void resolvingAbilityBoostsPower() {
        Permanent golem = addCreatureReady(player1, new HematiteGolem());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getEffectivePower()).isEqualTo(3);
        assertThat(golem.getEffectiveToughness()).isEqualTo(4);
        assertThat(golem.getPowerModifier()).isEqualTo(2);
        assertThat(golem.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly")
    void repeatedActivationsStack() {
        Permanent golem = addCreatureReady(player1, new HematiteGolem());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getPowerModifier()).isEqualTo(4);
        assertThat(golem.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent golem = addCreatureReady(player1, new HematiteGolem());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(golem.getPowerModifier()).isEqualTo(0);
        assertThat(golem.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new HematiteGolem());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability requires red mana even when enough generic mana is available")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new HematiteGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Golem can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new HematiteGolem());
        golem.setSummoningSick(true);
        golem.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(golem.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(golem.getEffectivePower()).isEqualTo(3);
        assertThat(golem.getEffectiveToughness()).isEqualTo(4);
        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability boosts only the Golem that activated it")
    void boostsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new HematiteGolem());
        Permanent other = addCreatureReady(player1, new HematiteGolem());
        Permanent opposing = addCreatureReady(player2, new HematiteGolem());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(opposing.getPowerModifier()).isZero();
    }
}
