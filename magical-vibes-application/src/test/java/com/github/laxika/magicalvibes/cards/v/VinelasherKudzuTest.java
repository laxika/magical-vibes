package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VinelasherKudzu.class, Forest.class})
class VinelasherKudzuTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a land you control enters")
    void putsCounterWhenControllerPlaysLand() {
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters")
    void doesNotTriggerForOpponentLand() {
        Permanent kudzu = harness.addToBattlefieldAndReturn(player1, new VinelasherKudzu());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(kudzu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
