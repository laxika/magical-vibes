package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FoodFight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Overcooked.class, FoodFight.class, GrizzlyBears.class, Forest.class, AngelOfMercy.class})
class OvercookedTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when celebration is not met")
    void createsFoodWithoutCelebration() {
        addOvercooked();

        advanceToEndStepAndResolve();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Food Fight")).isEmpty();
    }

    @Test
    @DisplayName("Conjures Food Fight after two nonland permanents enter")
    void conjuresFoodFightForCelebration() {
        addOvercooked();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToEndStepAndResolve();

        assertThat(findPermanents(player1, "Food Fight")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    @DisplayName("A land does not count toward celebration")
    void landDoesNotCountTowardCelebration() {
        addOvercooked();
        harness.enterBattlefieldAndReturn(player1, new Forest());

        advanceToEndStepAndResolve();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Food Fight")).isEmpty();
    }

    @Test
    @DisplayName("Players cannot gain life")
    void preventsLifeGain() {
        addOvercooked();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent permanents do not count toward celebration")
    void opponentEntriesDoNotCountTowardCelebration() {
        addOvercooked();
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStepAndResolve();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Food Fight")).isEmpty();
    }

    @Test
    @DisplayName("Celebration is checked when the end step trigger resolves")
    void celebrationCanBecomeMetAfterTriggering() {
        addOvercooked();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food Fight")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new Overcooked());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Food Fight")).isEmpty();
        assertThat(findPermanents(player2, "Food")).isEmpty();
    }

    @Test
    @DisplayName("Prevents the opponent from gaining life too")
    void preventsOpponentLifeGain() {
        harness.addToBattlefield(player1, new Overcooked());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Angel of Mercy");
    }

    @Test
    @DisplayName("Conjured Food Fight is a card rather than a token")
    void conjuresNontokenFoodFight() {
        addOvercooked();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStepAndResolve();

        assertThat(findPermanents(player1, "Food Fight")).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
        assertThat(findPermanents(player2, "Food Fight")).isEmpty();
    }

    @Test
    @DisplayName("Existing nonland permanents do not satisfy celebration")
    void existingPermanentsDoNotCountTowardCelebration() {
        harness.addToBattlefield(player1, new Overcooked());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToEndStepAndResolve();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Food Fight")).isEmpty();
    }

    private void addOvercooked() {
        harness.setHand(player1, List.of(new Overcooked()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToEndStepAndResolve() {
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
