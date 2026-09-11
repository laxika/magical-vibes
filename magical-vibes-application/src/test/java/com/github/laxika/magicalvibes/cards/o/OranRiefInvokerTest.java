package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OranRiefInvoker.class})
class OranRiefInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Eight mana gives Oran-Rief Invoker +5/+5 and trample until end of turn")
    void abilityBoostsSelfAndGrantsTrample() {
        Permanent invoker = addReadyInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(5);
        assertThat(invoker.getToughnessModifier()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        Permanent invoker = addReadyInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(0);
        assertThat(invoker.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without eight mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyInvoker() {
        return addCreatureReady(player1, new OranRiefInvoker());
    }
}
