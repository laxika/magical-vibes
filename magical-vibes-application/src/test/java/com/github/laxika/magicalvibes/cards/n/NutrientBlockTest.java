package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NutrientBlock.class, GrizzlyBears.class})
class NutrientBlockTest extends BaseCardTest {

    @Test
    void sacrificingItGainsThreeLifeAndDrawsACard() {
        harness.addToBattlefield(player1, new NutrientBlock());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, block));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Nutrient Block");
    }
}
