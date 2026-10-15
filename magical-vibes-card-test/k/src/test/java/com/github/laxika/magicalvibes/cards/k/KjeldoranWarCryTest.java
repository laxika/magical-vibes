package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranWarCry.class, KjeldoranOutrider.class})
class KjeldoranWarCryTest extends BaseCardTest {

    @Test
    @DisplayName("With no Kjeldoran War Cry in graveyards, gives your creatures +1/+1")
    void resolvesWithBaseBoost() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        harness.castFromHand(player1, new KjeldoranWarCry(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only matching Kjeldoran War Cry cards in all graveyards")
    void countsMatchingCardsInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new KjeldoranWarCry());
        gd.playerGraveyards.get(player2.getId()).add(new KjeldoranWarCry());
        gd.playerGraveyards.get(player1.getId()).add(new KjeldoranOutrider());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        harness.castFromHand(player1, new KjeldoranWarCry(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Only creatures you control get the boost")
    void onlyBoostsYourCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new KjeldoranOutrider());

        harness.castFromHand(player1, new KjeldoranWarCry(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(ownBear.getPowerModifier()).isEqualTo(1);
        assertThat(opposingBear.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Kjeldoran War Cry's boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        harness.castFromHand(player1, new KjeldoranWarCry(), "{1}{W}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A responding War Cry enters the graveyard before the original counts it")
    void countsCardsAtResolutionRatherThanCasting() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new KjeldoranOutrider());

        harness.castFromHand(player1, new KjeldoranWarCry(), "{1}{W}");
        harness.castFromHand(player2, new KjeldoranWarCry(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(opposingCreature.getPowerModifier()).isEqualTo(1);
        assertThat(opposingCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(ownCreature.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(2);
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(1);
        assertThat(opposingCreature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Successive War Cries stack their fixed boosts without counting themselves")
    void successiveCastsUseSeparateFixedAmounts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        harness.castFromHand(player1, new KjeldoranWarCry(), "{1}{W}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Kjeldoran War Cry");

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);

        harness.castFromHand(player1, new KjeldoranWarCry(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        harness.castFromHand(player1, new KjeldoranWarCry(), "{1}{W}");
        harness.passBothPriorities();
        KjeldoranOutrider laterCreature = new KjeldoranOutrider();
        harness.castFromHand(player1, laterCreature, "{1}{W}");
        harness.passBothPriorities();

        Permanent enteredCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(laterCreature.getId()))
                .findFirst().orElseThrow();
        assertThat(existingCreature.getPowerModifier()).isEqualTo(1);
        assertThat(existingCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(enteredCreature.getPowerModifier()).isZero();
        assertThat(enteredCreature.getToughnessModifier()).isZero();
    }
}
