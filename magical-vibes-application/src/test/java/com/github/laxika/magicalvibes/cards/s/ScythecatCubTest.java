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

    private void triggerLandfall(Permanent target) {
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
