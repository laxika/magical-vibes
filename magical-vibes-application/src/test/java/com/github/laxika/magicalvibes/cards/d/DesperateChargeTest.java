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

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ShuCavalry());

        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());

        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not boosted")
    void creaturesEnteringAfterResolutionAreNotBoosted() {
        Permanent creatureAlreadyOnBattlefield = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());

        harness.castFromHand(player1, new DesperateCharge(), "{2}{B}");
        harness.passBothPriorities();

        Permanent creatureEnteringLater = harness.addToBattlefieldAndReturn(player1, new ShuCavalry());

        assertThat(creatureAlreadyOnBattlefield.getEffectivePower()).isEqualTo(4);
        assertThat(creatureAlreadyOnBattlefield.getEffectiveToughness()).isEqualTo(2);
        assertThat(creatureEnteringLater.getEffectivePower()).isEqualTo(2);
        assertThat(creatureEnteringLater.getEffectiveToughness()).isEqualTo(2);
    }
}
