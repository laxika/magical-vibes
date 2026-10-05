package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AggravatedAssault;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NinjaPizza.class, AggravatedAssault.class})
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

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    void doesNotCreateFoodAtBeginningOfFirstMainPhase() {
        harness.addToBattlefield(player1, new NinjaPizza());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    void foodRetainsItsLifeGainAbility() {
        harness.addToBattlefield(player1, new NinjaPizza());
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent food = findPermanent(player1, "Food");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), 0, null, null);

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    void doesNotCreateFoodDuringOpponentsSecondMainPhase() {
        harness.addToBattlefield(player1, new NinjaPizza());

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player2, "Food")).isEmpty();
    }

    @Test
    void foodLosesGrantedManaAbilityWhenNinjaPizzaLeaves() {
        Permanent pizza = harness.addToBattlefieldAndReturn(player1, new NinjaPizza());
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(pizza);
        Permanent food = findPermanent(player1, "Food");

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(food), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
        assertThat(findPermanents(player1, "Food")).containsExactly(food);
    }

    @Test
    void doesNotGrantManaAbilityToOpponentsFood() {
        harness.addToBattlefield(player2, new NinjaPizza());
        advanceToPostcombatMain(player2);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard() instanceof NinjaPizza);
        harness.addToBattlefield(player1, new NinjaPizza());
        Permanent food = findPermanent(player2, "Food");

        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(food), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
        assertThat(findPermanents(player2, "Food")).containsExactly(food);
    }

    @Test
    void createsFoodEvenIfNinjaPizzaLeavesAfterTriggering() {
        Permanent pizza = harness.addToBattlefieldAndReturn(player1, new NinjaPizza());
        advanceToPostcombatMain(player1);
        gd.playerBattlefields.get(player1.getId()).remove(pizza);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void doesNotCreateAnotherFoodDuringThirdMainPhase() {
        harness.addToBattlefield(player1, new NinjaPizza());
        harness.addToBattlefield(player1, new AggravatedAssault());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Food")).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
    }
}
