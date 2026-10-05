package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KjeldoranOutrider.class)
class KjeldoranOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent outrider = addReadyOutrider();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(outrider.getPowerModifier()).isEqualTo(0);
        assertThat(outrider.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated multiple times in one turn")
    void abilityCanBeActivatedMultipleTimes() {
        Permanent outrider = addReadyOutrider();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(outrider.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated while the creature has summoning sickness")
    void abilityCanBeActivatedWithSummoningSickness() {
        Permanent outrider = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(outrider.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without white mana")
    void cannotActivateWithoutMana() {
        addReadyOutrider();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent outrider = addReadyOutrider();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(outrider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A tapped Outrider can activate its ability without untapping")
    void abilityCanBeActivatedWhileTapped() {
        Permanent outrider = addReadyOutrider();
        outrider.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(outrider.getToughnessModifier()).isEqualTo(1);
        assertThat(outrider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost waits for resolution and affects only the activating Outrider")
    void boostUsesStackAndOnlyAffectsSource() {
        Permanent otherOutrider = addReadyOutrider();
        Permanent source = addReadyOutrider();
        Permanent opposingOutrider = harness.addToBattlefieldAndReturn(player2, new KjeldoranOutrider());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(source.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(0);
        assertThat(source.getToughnessModifier()).isEqualTo(1);
        assertThat(otherOutrider.getToughnessModifier()).isEqualTo(0);
        assertThat(opposingOutrider.getToughnessModifier()).isEqualTo(0);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the white activation cost")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent outrider = addReadyOutrider();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(outrider.getToughnessModifier()).isEqualTo(0);
    }
    private Permanent addReadyOutrider() {
        return addCreatureReady(player1, new KjeldoranOutrider());
    }
}
