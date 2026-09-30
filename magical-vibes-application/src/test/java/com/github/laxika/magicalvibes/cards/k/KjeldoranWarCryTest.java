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
}
