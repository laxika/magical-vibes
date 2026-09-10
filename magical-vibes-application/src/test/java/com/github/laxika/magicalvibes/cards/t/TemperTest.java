package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfEssence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Temper.class, WallOfEssence.class, Shock.class})
class TemperTest extends BaseCardTest {

    @Test
    void preventsDamageAndImmediatelyAddsPlusOneCounters() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfEssence());
        harness.setHand(player1, List.of(new Temper()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, 3, wall.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    void preventsOnlyThePaidAmountAndCountersEachPreventedDamage() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfEssence());
        harness.setHand(player1, List.of(new Temper()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, 2, wall.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canTargetAnOpponentsCreature() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfEssence());
        harness.setHand(player1, List.of(new Temper()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, 1, wall.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(wall.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void zeroXDoesNotPreventDamageOrAddCounters() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfEssence());
        harness.setHand(player1, List.of(new Temper()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 0, wall.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void preventionAndCountersExpireAtEndOfTurn() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfEssence());
        harness.setHand(player1, List.of(new Temper()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, 2, wall.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new Temper()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
