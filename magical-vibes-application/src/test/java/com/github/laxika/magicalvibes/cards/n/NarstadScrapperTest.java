package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NarstadScrapper.class})
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

    @Test
    @DisplayName("A tapped, summoning-sick Scrapper can activate without tapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent scrapper = harness.addToBattlefieldAndReturn(player1, new NarstadScrapper());
        scrapper.setSummoningSick(true);
        scrapper.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(scrapper.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(scrapper.getPowerModifier()).isEqualTo(1);
        assertThat(scrapper.getToughnessModifier()).isZero();
        assertThat(scrapper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each activation consumes two mana")
    void activationConsumesMana() {
        Permanent scrapper = addCreatureReady(player1, new NarstadScrapper());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scrapper.getPowerModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating one Scrapper does not boost another copy")
    void boostsOnlyItsSource() {
        Permanent first = addCreatureReady(player1, new NarstadScrapper());
        Permanent second = addCreatureReady(player1, new NarstadScrapper());
        Permanent opposing = addCreatureReady(player2, new NarstadScrapper());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isZero();
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(opposing.getPowerModifier()).isZero();
    }
}
