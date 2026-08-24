package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({RazzleDazzler.class, Shock.class})
class RazzleDazzlerTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell puts a counter on Razzle-Dazzler and makes it unblockable until end of turn")
    void secondSpellPutsCounterAndMakesUnblockableUntilEndOfTurn() {
        Permanent dazzler = addCreatureReady(player1, new RazzleDazzler());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dazzler.isCantBeBlocked()).isFalse();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isTrue();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dazzler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dazzler.isCantBeBlocked()).isFalse();
    }
}
