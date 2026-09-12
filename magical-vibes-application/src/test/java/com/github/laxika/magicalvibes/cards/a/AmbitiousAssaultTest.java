package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbitiousAssault.class, GrizzlyBears.class})
class AmbitiousAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures and draws if you control a modified creature")
    void boostsAndDrawsWithModifiedCreature() {
        Permanent modifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        modifiedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new AmbitiousAssault()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, modifiedBear)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, unmodifiedBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw without a modified creature")
    void doesNotDrawWithoutModifiedCreature() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AmbitiousAssault()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AmbitiousAssault()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }
}
