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
        harness.addToBattlefield(player1, new Forest());

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

    private void addOvercooked() {
        harness.setHand(player1, List.of(new Overcooked()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToEndStepAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
