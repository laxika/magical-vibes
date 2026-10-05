package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({NessianAsp.class})
class NessianAspTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts four +1/+1 counters on Nessian Asp")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent asp = addReadyAsp();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(asp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(asp.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("An already monstrous Nessian Asp can activate monstrosity again without gaining counters")
    void alreadyMonstrousAspCanActivateAgain() {
        Permanent asp = addReadyAsp();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatCode(() -> harness.activateAbility(player1, 0, null, null))
                .doesNotThrowAnyException();
        harness.passBothPriorities();

        assertThat(asp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(asp.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Two pending monstrosity activations add counters only once")
    void twoPendingActivationsOnlyAddFourCounters() {
        Permanent asp = addReadyAsp();
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(asp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(asp.isMonstrous()).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(asp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(asp.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Monstrosity can be activated while Nessian Asp is summoning sick and tapped")
    void monstrosityDoesNotRequireAnUntappedReadyCreature() {
        Permanent asp = harness.addToBattlefieldAndReturn(player1, new NessianAsp());
        asp.setSummoningSick(true);
        asp.setTapped(true);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(asp.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(asp.isMonstrous()).isTrue();
        assertThat(asp.isTapped()).isTrue();
    }

    private Permanent addReadyAsp() {
        Permanent asp = harness.addToBattlefieldAndReturn(player1, new NessianAsp());
        asp.setSummoningSick(false);
        return asp;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}

