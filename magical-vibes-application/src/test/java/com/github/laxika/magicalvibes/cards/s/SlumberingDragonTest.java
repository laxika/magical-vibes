package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlumberingDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack with fewer than five +1/+1 counters")
    void cannotAttackWithoutFiveCounters() {
        Permanent dragon = addCreatureReady(player1, new SlumberingDragon());
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with five or more +1/+1 counters")
    void canAttackWithFiveCounters() {
        harness.setLife(player2, 20);
        Permanent dragon = addCreatureReady(player1, new SlumberingDragon());
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot block with fewer than five +1/+1 counters")
    void cannotBlockWithoutFiveCounters() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new SlumberingDragon());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block with five or more +1/+1 counters")
    void canBlockWithFiveCounters() {
        addCreatureReady(player2, new AxegrinderGiant());
        Permanent dragon = addCreatureReady(player1, new SlumberingDragon());
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(dragon.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature attacking its controller puts a +1/+1 counter on it")
    void gainsCounterWhenAttacked() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent dragon = addCreatureReady(player1, new SlumberingDragon());

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
