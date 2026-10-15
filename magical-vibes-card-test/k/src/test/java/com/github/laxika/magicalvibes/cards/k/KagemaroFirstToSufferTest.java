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

    @Test
    @DisplayName("The ability does nothing if its controller has no cards when it resolves")
    void emptyHandAtResolutionGivesNoPenalty() {
        harness.setHand(player1, List.of(new HandOfHonor()));
        harness.addToBattlefield(player1, new KagemaroFirstToSuffer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KamiOfTheTendedGarden());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Kagemaro, First to Suffer");
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Kami of the Tended Garden");
    }

    @Test
    @DisplayName("Protection from black does not prevent the untargeted toughness reduction")
    void killsCreaturesWithProtectionFromBlack() {
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        harness.addToBattlefield(player1, new KagemaroFirstToSuffer());
        harness.addToBattlefield(player1, new HandOfHonor());
        harness.addToBattlefield(player2, new HandOfHonor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hand of Honor");
        harness.assertNotOnBattlefield(player2, "Hand of Honor");
        harness.assertInGraveyard(player1, "Hand of Honor");
        harness.assertInGraveyard(player2, "Hand of Honor");
    }

    @Test
    @DisplayName("The resolved penalty stays fixed and does not affect later creatures")
    void penaltyIsFixedAtResolutionAndOnlyAffectsExistingCreatures() {
        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        harness.addToBattlefield(player1, new KagemaroFirstToSuffer());
        Permanent original = harness.addToBattlefieldAndReturn(player2, new KamiOfTheTendedGarden());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new HandOfHonor()));
        Permanent later = harness.enterBattlefieldAndReturn(player2, new KamiOfTheTendedGarden());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(4);
    }

    @Test
    @DisplayName("Kagemaro's characteristic defining ability works in hand and graveyard")
    void powerAndToughnessTrackHandSizeOutsideBattlefield() {
        KagemaroFirstToSuffer kagemaro = new KagemaroFirstToSuffer();
        harness.setHand(player1, List.of(kagemaro, new HandOfHonor(), new HandOfHonor()));
        harness.setHand(player2, List.of(new HandOfHonor()));

        assertThat(gqs.getEffectiveCardPower(gd, kagemaro)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, kagemaro)).isEqualTo(3);

        harness.setHand(player1, List.of(new HandOfHonor(), new HandOfHonor()));
        harness.setGraveyard(player1, List.of(kagemaro));

        assertThat(gqs.getEffectiveCardPower(gd, kagemaro)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, kagemaro)).isEqualTo(2);

        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectiveCardPower(gd, kagemaro)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, kagemaro)).isZero();
    }
}
