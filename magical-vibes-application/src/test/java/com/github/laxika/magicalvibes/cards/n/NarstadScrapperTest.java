package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NarstadScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} gives Narstad Scrapper +1/+0")
    void abilityBoostsItself() {
        Permanent scrapper = addCreatureReady(player1, new NarstadScrapper());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scrapper.getPowerModifier()).isEqualTo(1);
        assertThat(scrapper.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly and the boosts stack")
    void boostsStack() {
        Permanent scrapper = addCreatureReady(player1, new NarstadScrapper());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scrapper.getPowerModifier()).isEqualTo(2);
        assertThat(scrapper.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent scrapper = addCreatureReady(player1, new NarstadScrapper());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(scrapper.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(scrapper.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability cannot be activated without mana")
    void abilityRequiresMana() {
        addCreatureReady(player1, new NarstadScrapper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
