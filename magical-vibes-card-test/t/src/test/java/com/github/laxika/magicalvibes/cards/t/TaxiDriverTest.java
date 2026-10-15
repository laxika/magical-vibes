package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({TaxiDriver.class, GrizzlyBears.class, Mountain.class})
class TaxiDriverTest extends BaseCardTest {

    @Test
    @DisplayName("Pays one mana and taps to give a target creature haste")
    void grantsHasteToTargetCreature() {
        Permanent driver = addReadyTaxiDriver();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(driver.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        addReadyTaxiDriver();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Ability can only target creatures")
    void cannotTargetNonCreature() {
        addReadyTaxiDriver();
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can grant haste to an opponent's creature without granting it to the source")
    void grantsHasteToOpponentsCreature() {
        Permanent driver = addReadyTaxiDriver();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TaxiDriver());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, driver, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent driver = addReadyTaxiDriver();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, driver.getId());
        harness.passBothPriorities();

        assertThat(driver.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, driver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without paying one mana")
    void cannotActivateWithoutMana() {
        Permanent driver = addReadyTaxiDriver();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, driver.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(driver.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent driver = addReadyTaxiDriver();
        driver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, driver.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate its tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent driver = harness.addToBattlefieldAndReturn(player1, new TaxiDriver());
        driver.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, driver.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(driver.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyTaxiDriver() {
        Permanent driver = harness.addToBattlefieldAndReturn(player1, new TaxiDriver());
        driver.setSummoningSick(false);
        return driver;
    }
}
