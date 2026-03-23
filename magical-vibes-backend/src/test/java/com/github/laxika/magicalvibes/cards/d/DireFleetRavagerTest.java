package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DireFleetRavagerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Dire Fleet Ravager has ETB effect that loses a third of life rounded up")
    void hasEtbEffect() {
        DireFleetRavager card = new DireFleetRavager();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(EachPlayerLosesFractionOfLifeRoundedUpEffect.class);

        EachPlayerLosesFractionOfLifeRoundedUpEffect effect =
                (EachPlayerLosesFractionOfLifeRoundedUpEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.divisor()).isEqualTo(3);
    }

    // ===== ETB trigger behavior =====

    @Test
    @DisplayName("Each player loses a third of their life rounded up on ETB")
    void eachPlayerLosesThirdOfLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DireFleetRavager()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (puts ETB on stack)
        harness.passBothPriorities(); // resolve ETB trigger

        // ceil(20/3) = 7, so each player goes from 20 to 13
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Rounds up correctly when life is not divisible by 3")
    void roundsUpCorrectly() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        harness.setHand(player1, List.of(new DireFleetRavager()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // ceil(10/3) = 4, player1: 10 - 4 = 6
        // ceil(7/3) = 3, player2: 7 - 3 = 4
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Works correctly when life is exactly divisible by 3")
    void exactlyDivisibleByThree() {
        harness.setLife(player1, 9);
        harness.setLife(player2, 12);
        harness.setHand(player1, List.of(new DireFleetRavager()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // ceil(9/3) = 3, player1: 9 - 3 = 6
        // ceil(12/3) = 4, player2: 12 - 4 = 8
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Works with low life totals")
    void worksWithLowLife() {
        harness.setLife(player1, 1);
        harness.setLife(player2, 2);
        harness.setHand(player1, List.of(new DireFleetRavager()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // ceil(1/3) = 1, player1: 1 - 1 = 0
        // ceil(2/3) = 1, player2: 2 - 1 = 1
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1);
    }
}
