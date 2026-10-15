package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarriorsResolve.class, GrizzlyBears.class, TrainedArmodon.class, Forest.class})
class WarriorsResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Training adds a counter and the end-step ability draws a card")
    void trainsAndDrawsAtEndStep() {
        harness.addToBattlefield(player1, new WarriorsResolve());
        Permanent trainee = addCreatureReady(player1, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new TrainedArmodon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(trainee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when no creature with a counter attacked this turn")
    void doesNotDrawWithoutAttackedCounteredCreature() {
        harness.addToBattlefield(player1, new WarriorsResolve());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not train without a greater-power attacking creature")
    void doesNotTrainWithoutGreaterPowerAttacker() {
        harness.addToBattlefield(player1, new WarriorsResolve());
        Permanent trainee = addCreatureReady(player1, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));

        assertThat(gd.stack).isEmpty();
        assertThat(trainee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
