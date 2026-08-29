package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CaravanEscort;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeneratedTeacherTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability puts level counters only on creatures with level up")
    void putsLevelCountersOnLevelUpCreatures() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingEscort = harness.addToBattlefieldAndReturn(player2, new CaravanEscort());

        harness.setHand(player1, List.of(new VeneratedTeacher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(escort.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(opposingEscort.getCounterCount(CounterType.LEVEL)).isZero();
    }
}
