package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfThePilgrimsRoad.class, YokedOx.class})
class KnightOfThePilgrimsRoadTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent knight = addCreatureReady(player1, new KnightOfThePilgrimsRoad());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent knight = addCreatureReady(player1, new KnightOfThePilgrimsRoad());
        knight.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Renown does not trigger when the creature is blocked")
    void noRenownWhenBlocked() {
        Permanent knight = addCreatureReady(player1, new KnightOfThePilgrimsRoad());
        addCreatureReady(player2, new YokedOx());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(knight.isRenowned()).isFalse();
    }

    @Test
    @DisplayName("Renown works when the second player attacks")
    void renownForSecondPlayer() {
        Permanent knight = addCreatureReady(player2, new KnightOfThePilgrimsRoad());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Removing the renown counter does not allow renown to trigger again")
    void remainsRenownedWithoutCounters() {
        Permanent knight = addCreatureReady(player1, new KnightOfThePilgrimsRoad());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();
        assertThat(knight.isRenowned()).isTrue();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.performUntapStep(player1);
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(knight.isRenowned()).isTrue();
    }
}
