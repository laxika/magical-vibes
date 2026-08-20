package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BalduvianFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep boosts Balduvian Fallen for black and red mana spent")
    void payingCumulativeUpkeepBoostsForBlackAndRedMana() {
        Permanent fallen = harness.addToBattlefieldAndReturn(player1, new BalduvianFallen());
        fallen.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(fallen.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(fallen.getPowerModifier()).isEqualTo(2);
        assertThat(fallen.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep boost does not count colorless mana and wears off at cleanup")
    void boostCountsOnlyBlackAndRedAndWearsOff() {
        Permanent fallen = harness.addToBattlefieldAndReturn(player1, new BalduvianFallen());
        fallen.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(fallen.getPowerModifier()).isEqualTo(1);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fallen.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Balduvian Fallen")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent fallen = harness.addToBattlefieldAndReturn(player1, new BalduvianFallen());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fallen);
        harness.assertInGraveyard(player1, "Balduvian Fallen");
    }
}
