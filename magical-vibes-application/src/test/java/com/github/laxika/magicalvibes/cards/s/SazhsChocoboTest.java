package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SazhsChocobo.class, Forest.class})
class SazhsChocoboTest extends BaseCardTest {

    @Test
    void landfallPutsCounterOnSazhsChocobo() {
        Permanent chocobo = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(chocobo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentLandfallDoesNotTriggerSazhsChocobo() {
        Permanent chocobo = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(chocobo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
