package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoonwingMoth.class)
class MoonwingMothTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts itself +0/+1 until end of turn")
    void boostsSelf() {
        Permanent moth = addCreatureReady(player1, new MoonwingMoth());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(moth.getPowerModifier()).isZero();
        assertThat(moth.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not tap Moonwing Moth when its ability is activated")
    void doesNotTapOnActivation() {
        Permanent moth = addCreatureReady(player1, new MoonwingMoth());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(moth.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent moth = addCreatureReady(player1, new MoonwingMoth());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(moth.getPowerModifier()).isZero();
        assertThat(moth.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can activate the ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent moth = addCreatureReady(player1, new MoonwingMoth());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(moth.getPowerModifier()).isZero();
        assertThat(moth.getToughnessModifier()).isEqualTo(2);
    }
}
