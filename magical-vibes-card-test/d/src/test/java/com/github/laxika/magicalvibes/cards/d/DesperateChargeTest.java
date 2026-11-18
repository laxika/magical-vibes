package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperateCharge.class, ShuCavalry.class})
class DesperateChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts all own creatures +2/+0")
    void resolvingBoostsAllOwnCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());

        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ShuCavalry());

        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());

        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not boosted")
    void creaturesEnteringAfterResolutionAreNotBoosted() {
        Permanent creatureAlreadyOnBattlefield = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());

        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();

        Permanent creatureEnteringLater = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());

        assertThat(gqs.getEffectivePower(gd, creatureAlreadyOnBattlefield)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creatureAlreadyOnBattlefield)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creatureEnteringLater)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creatureEnteringLater)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering before resolution are boosted")
    void creaturesEnteringBeforeResolutionAreBoosted() {
        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple casts stack and both boosts expire at cleanup")
    void multipleCastsStackUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());

        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
