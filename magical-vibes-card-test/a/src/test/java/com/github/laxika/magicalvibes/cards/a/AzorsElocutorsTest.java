package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzorsElocutors.class, AnnihilatingFire.class})
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
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Damage from one source removes one filibuster counter")
    void damageRemovesOneCounterPerSource() {
        Permanent elocutors = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        elocutors.setCounterCount(CounterType.FILIBUSTER, 3);
        harness.setHand(player2, List.of(new AnnihilatingFire()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());
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
    @Test
    void opponentUpkeepDoesNotAddCounterOrWin() {
        Permanent elocutors = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        elocutors.setCounterCount(CounterType.FILIBUSTER, 5);

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(elocutors.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(5);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void damageInResponseToUpkeepPreventsFifthCounterWin() {
        Permanent elocutors = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        elocutors.setCounterCount(CounterType.FILIBUSTER, 4);
        harness.setHand(player2, List.of(new AnnihilatingFire()));

        advanceToUpkeep(player1);
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(elocutors.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(elocutors.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(4);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void damageToOpponentDoesNotRemoveCounter() {
        Permanent elocutors = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        elocutors.setCounterCount(CounterType.FILIBUSTER, 3);
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(elocutors.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageFromOwnSourceTriggersEvenWithoutCounters() {
        Permanent elocutors = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(elocutors.getCounterCount(CounterType.FILIBUSTER)).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    void countersOnDifferentCopiesDoNotCombine() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AzorsElocutors());
        first.setCounterCount(CounterType.FILIBUSTER, 2);
        second.setCounterCount(CounterType.FILIBUSTER, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.FILIBUSTER)).isEqualTo(3);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
