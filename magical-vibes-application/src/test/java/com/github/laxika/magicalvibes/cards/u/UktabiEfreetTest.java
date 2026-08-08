package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The third upkeep costs {G}{G}{G}")
    void thirdUpkeepCostsThreeGreen() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new UktabiEfreet());
        efreet.setCounterCount(CounterType.AGE, 2);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.passBothPriorities();

        assertThat(efreet.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
        assertThat(gd.gameLog).anyMatch(entry ->
                entry.plainText().contains("pays {G}{G}{G}."));
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Uktabi Efreet")
    void decliningUpkeepSacrifices() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new UktabiEfreet());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);
        harness.assertInGraveyard(player1, "Uktabi Efreet");
    }
}
