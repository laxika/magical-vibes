package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MarbleGargoyle.class)
class MarbleGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Marble Gargoyle +0/+1")
    void resolvingAbilityBoostsToughness() {
        addCreatureReady(player1, new MarbleGargoyle());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent gargoyle = findPermanent(player1, "Marble Gargoyle");
        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(3);
        assertThat(gargoyle.getPowerModifier()).isZero();
        assertThat(gargoyle.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated multiple times if mana allows")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new MarbleGargoyle());
        harness.addMana(player1, ManaColor.WHITE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        Permanent gargoyle = findPermanent(player1, "Marble Gargoyle");
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(5);
        assertThat(gargoyle.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The toughness boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new MarbleGargoyle());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent gargoyle = findPermanent(player1, "Marble Gargoyle");
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gargoyle.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(2);
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new MarbleGargoyle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
