package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuelingCoachTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DuelingCoach()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void etbCannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DuelingCoach()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability puts counters only on controlled creatures that already have one")
    void activatedAbilityPutsCountersOnExistingCounterBearers() {
        Permanent coach = harness.addToBattlefieldAndReturn(player1, new DuelingCoach());
        Permanent withCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent withoutCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(coach.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(withCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(withoutCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
