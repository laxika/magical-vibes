package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZephyrSinger.class, GrizzlyBears.class})
class ZephyrSingerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a flying counter on each creature that convoked it")
    void putsFlyingCountersOnConvokeCreatures() {
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent nonConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ZephyrSinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstConvokeCreature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(secondConvokeCreature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(firstConvokeCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(nonConvokeCreature.getCounterCount(CounterType.FLYING)).isZero();
    }
}
