package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IllusionaryWall.class)
class IllusionaryWallTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusionary Wall")
    void paysCumulativeUpkeep() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
    }

    @Test
    @DisplayName("Cumulative upkeep costs two blue mana on the second upkeep")
    void cumulativeUpkeepCostIncreases() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Partial cumulative upkeep payment is not accepted")
    void partialPaymentSacrificesWithoutSpendingMana() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());
        wall.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player1, "Illusionary Wall");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusionary Wall")
    void declineSacrifices() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new IllusionaryWall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player1, "Illusionary Wall");
    }
}
