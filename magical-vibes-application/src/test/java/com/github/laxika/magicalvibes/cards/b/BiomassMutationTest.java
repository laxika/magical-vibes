package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BiomassMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving with X=4 sets base power/toughness of your creatures to 4/4")
    void setsOwnCreaturesToXX() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Base P/T is set: a +1/+1 counter still applies on top of the new base")
    void modifiersApplyOnTopOfNewBase() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only affects creatures you control, not the opponent's")
    void doesNotAffectOpponentCreatures() {
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
