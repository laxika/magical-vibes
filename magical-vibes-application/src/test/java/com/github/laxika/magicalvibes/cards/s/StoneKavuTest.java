package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoneKavu.class})
class StoneKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability gives +1/+0 until end of turn")
    void redAbilityBoostsPower() {
        Permanent kavu = addCreatureReady(player1, new StoneKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(1);
        assertThat(kavu.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("White ability gives +0/+1 until end of turn")
    void whiteAbilityBoostsToughness() {
        Permanent kavu = addCreatureReady(player1, new StoneKavu());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isZero();
        assertThat(kavu.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both abilities can be activated repeatedly")
    void abilitiesStack() {
        Permanent kavu = addCreatureReady(player1, new StoneKavu());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(1);
        assertThat(kavu.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each ability can be activated more than once in a turn")
    void eachAbilityCanBeActivatedRepeatedly() {
        Permanent kavu = addCreatureReady(player1, new StoneKavu());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(2);
        assertThat(kavu.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both abilities can be activated while the Kavu is tapped")
    void abilitiesDoNotRequireTapping() {
        Permanent kavu = addCreatureReady(player1, new StoneKavu());
        kavu.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kavu.isTapped()).isTrue();
        assertThat(kavu.getPowerModifier()).isEqualTo(1);
        assertThat(kavu.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent kavu = addCreatureReady(player1, new StoneKavu());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isZero();
        assertThat(kavu.getToughnessModifier()).isZero();
    }
}
