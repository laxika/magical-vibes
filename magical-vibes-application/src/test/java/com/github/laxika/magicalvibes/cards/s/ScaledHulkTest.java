package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.m.MatsuTribeSniper;
import com.github.laxika.magicalvibes.cards.r.RoarOfJukai;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScaledHulk.class, Frostling.class, RoarOfJukai.class, MatsuTribeSniper.class})
class ScaledHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell gives Scaled Hulk +2/+2")
    void spiritSpellBoostsScaledHulk() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.castFromHand(player1, new Frostling(), "{R}");
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(2);
        assertThat(hulk.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting an Arcane spell gives Scaled Hulk +2/+2")
    void arcaneSpellBoostsScaledHulk() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.castFromHand(player1, new RoarOfJukai(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(2);
        assertThat(hulk.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger Scaled Hulk")
    void unrelatedSpellDoesNotBoostScaledHulk() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.castFromHand(player1, new MatsuTribeSniper(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isZero();
        assertThat(hulk.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boosts stack and wear off at end of turn")
    void boostsStackAndWearOffAtEndOfTurn() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.castFromHand(player1, new RoarOfJukai(), "{2}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new RoarOfJukai(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(4);
        assertThat(hulk.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(hulk.getPowerModifier()).isZero();
        assertThat(hulk.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's Spirit spell does not trigger Scaled Hulk")
    void opponentSpiritSpellDoesNotBoostScaledHulk() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Frostling(), "{R}");
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isZero();
        assertThat(hulk.getToughnessModifier()).isZero();
    }
}
