package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(NutrientBlock.class)
class NutrientBlockTest extends BaseCardTest {

    @Test
    void sacrificingItGainsThreeLifeAndDrawsACard() {
        harness.addToBattlefield(player1, new NutrientBlock());
        harness.setLibrary(player1, List.of(new NutrientBlock()));
        harness.setLife(player1, 20);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Nutrient Block");
    }

    @Test
    void drawsWhenPutIntoGraveyardFromBattlefield() {
        Permanent block = harness.addToBattlefieldAndReturn(player1, new NutrientBlock());
        harness.setLibrary(player1, List.of(new NutrientBlock()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, block));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Nutrient Block");
    }

    @Test
    void sacrificeIsPaidImmediatelyAndDrawResolvesBeforeLifeGain() {
        harness.addToBattlefield(player1, new NutrientBlock());
        harness.setLibrary(player1, List.of(new NutrientBlock()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Nutrient Block");
        harness.assertInGraveyard(player1, "Nutrient Block");
        harness.assertNotInHand(player1, "Nutrient Block");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Nutrient Block");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    void tappedBlockCannotPayItsActivationCost() {
        Permanent block = harness.addToBattlefieldAndReturn(player1, new NutrientBlock());
        block.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nutrient Block");
        harness.assertNotInGraveyard(player1, "Nutrient Block");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresTwoMana() {
        harness.addToBattlefield(player1, new NutrientBlock());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nutrient Block");
        harness.assertNotInGraveyard(player1, "Nutrient Block");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilingBlockDoesNotTriggerDraw() {
        Permanent block = harness.addToBattlefieldAndReturn(player1, new NutrientBlock());
        harness.setLibrary(player1, List.of(new NutrientBlock()));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, block));

        harness.assertNotOnBattlefield(player1, "Nutrient Block");
        harness.assertNotInGraveyard(player1, "Nutrient Block");
        harness.assertNotInHand(player1, "Nutrient Block");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerDrawsAndGainsLifeWhileOwnerReceivesSacrificedCard() {
        NutrientBlock block = new NutrientBlock();
        block.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, block);
        harness.setLibrary(player1, List.of(new NutrientBlock()));
        harness.setLibrary(player2, List.of(new NutrientBlock()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nutrient Block");
        harness.assertNotInHand(player2, "Nutrient Block");
        harness.assertInGraveyard(player2, "Nutrient Block");
        harness.assertNotInGraveyard(player1, "Nutrient Block");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }
}
