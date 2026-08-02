package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AzorsElocutorsTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a filibuster counter at upkeep and wins at five counters")
    void addsCounterAndWinsAtFive() {
        Permanent elocutors = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        elocutors.setCounterCount(CounterType.FILIBUSTER, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(elocutors.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(5);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Damage from one source removes one filibuster counter")
    void damageRemovesOneCounterPerSource() {
        Permanent elocutors = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        elocutors.setCounterCount(CounterType.FILIBUSTER, 3);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elocutors.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not win with fewer than five counters")
    void doesNotWinBelowFiveCounters() {
        Permanent elocutors = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        elocutors.setCounterCount(CounterType.FILIBUSTER, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(elocutors.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(4);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
