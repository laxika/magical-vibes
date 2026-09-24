package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NinjaPizza.class)
class NinjaPizzaTest extends BaseCardTest {

    @Test
    void createsFoodAtBeginningOfSecondMainPhase() {
        harness.addToBattlefield(player1, new NinjaPizza());

        advanceToPostcombatMain(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void foodCanBeSacrificedForAnyColorMana() {
        harness.addToBattlefield(player1, new NinjaPizza());

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        Permanent food = findPermanents(player1, "Food").getFirst();
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.activateAbility(player1, foodIndex, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    void doesNotCreateFoodAtBeginningOfFirstMainPhase() {
        harness.addToBattlefield(player1, new NinjaPizza());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
