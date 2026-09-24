package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KagemaroFirstToSuffer.class, KamiOfTheTendedGarden.class, HandOfHonor.class})
class KagemaroFirstToSufferTest extends BaseCardTest {

    @Test
    @DisplayName("Kagemaro's power and toughness equal the cards in its controller's hand")
    void powerAndToughnessEqualControllerHandSize() {
        Permanent kagemaro = addCreatureReady(player1, new KagemaroFirstToSuffer());
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));
        harness.setHand(player2, List.of(new HandOfHonor(), new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));

        assertThat(gqs.getEffectivePower(gd, kagemaro)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kagemaro)).isEqualTo(3);

        harness.setHand(player1, List.of(new HandOfHonor()));

        assertThat(gqs.getEffectivePower(gd, kagemaro)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kagemaro)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Kagemaro gives all creatures -X/-X based on hand size")
    void sacrificesKagemaroAndWeakensAllCreatures() {
        harness.addToBattlefield(player1, new KagemaroFirstToSuffer());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new KamiOfTheTendedGarden());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new KamiOfTheTendedGarden());
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kagemaro, First to Suffer");
        harness.assertInGraveyard(player1, "Kagemaro, First to Suffer");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -X/-X effect wears off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new KagemaroFirstToSuffer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KamiOfTheTendedGarden());
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability uses the hand size when it resolves")
    void usesHandSizeAtResolution() {
        harness.addToBattlefield(player1, new KagemaroFirstToSuffer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KamiOfTheTendedGarden());
        harness.setHand(player1, List.of(new HandOfHonor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }
}
