package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UktabiEfreet.class})
class UktabiEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the cumulative upkeep costs {G} per age counter")
    void paysCumulativeUpkeep() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new UktabiEfreet());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        assertThat(efreet.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Uktabi Efreet");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The third upkeep costs {G}{G}{G}")
    void thirdUpkeepCostsThreeGreen() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new UktabiEfreet());
        efreet.setCounterCount(CounterType.AGE, 2);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.passBothPriorities();

        assertThat(efreet.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Uktabi Efreet");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.gameLog).anyMatch(entry ->
                entry.plainText().contains("pays {G}{G}{G}."));
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during Uktabi Efreet's controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new UktabiEfreet());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(efreet.getCounterCount(CounterType.AGE)).isZero();
        harness.assertOnBattlefield(player1, "Uktabi Efreet");
    }

    @Test
    @DisplayName("Cumulative upkeep cannot be partially paid")
    void partialPaymentSacrificesWithoutSpendingMana() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new UktabiEfreet());
        efreet.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(efreet.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Uktabi Efreet");
        harness.assertInGraveyard(player1, "Uktabi Efreet");
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Uktabi Efreet")
    void decliningUpkeepSacrifices() {
        harness.addToBattlefield(player1, new UktabiEfreet());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Uktabi Efreet");
        harness.assertInGraveyard(player1, "Uktabi Efreet");
    }
}
