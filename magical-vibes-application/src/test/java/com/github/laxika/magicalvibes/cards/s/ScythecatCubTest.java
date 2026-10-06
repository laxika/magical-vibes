package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScythecatCub.class, Forest.class})
class ScythecatCubTest extends BaseCardTest {

    @Test
    @DisplayName("The first landfall puts a +1/+1 counter on the target creature")
    void firstLandfallPutsCounterOnTarget() {
        Permanent cub = addCreatureReady(player1, new ScythecatCub());

        triggerLandfall(cub);

        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second landfall doubles counters instead of adding one")
    void secondLandfallDoublesCountersInsteadOfAddingOne() {
        Permanent cub = addCreatureReady(player1, new ScythecatCub());

        triggerLandfall(cub);
        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        triggerLandfall(cub);

        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A landfall after the second one puts a counter normally")
    void thirdLandfallPutsCounterNormally() {
        Permanent cub = addCreatureReady(player1, new ScythecatCub());

        triggerLandfall(cub);
        triggerLandfall(cub);
        triggerLandfall(cub);

        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The second resolution doubles all existing counters without first adding one")
    void secondResolutionDoublesExistingCounters() {
        Permanent cub = addCreatureReady(player1, new ScythecatCub());
        cub.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        triggerLandfall(cub);
        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        triggerLandfall(cub);

        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Doubling zero counters on the second resolution adds no counters")
    void secondResolutionWithNoCountersAddsNothing() {
        Permanent cub = addCreatureReady(player1, new ScythecatCub());
        triggerLandfall(cub);
        cub.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        triggerLandfall(cub);

        assertThat(cub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A newly entered Cub starts its own resolution count")
    void newCubStartsItsOwnResolutionCount() {
        Permanent firstCub = addCreatureReady(player1, new ScythecatCub());
        triggerLandfall(firstCub);
        triggerLandfall(firstCub);
        gd.playerBattlefields.get(player1.getId()).remove(firstCub);
        Permanent newCub = addCreatureReady(player1, new ScythecatCub());

        triggerLandfall(newCub);

        assertThat(newCub.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    private void triggerLandfall(Permanent target) {
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
