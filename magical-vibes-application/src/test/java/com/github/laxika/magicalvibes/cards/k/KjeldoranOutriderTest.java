package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyOutrider() {
        Permanent outrider = new Permanent(new KjeldoranOutrider());
        outrider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(outrider);
        return outrider;
    }
}
