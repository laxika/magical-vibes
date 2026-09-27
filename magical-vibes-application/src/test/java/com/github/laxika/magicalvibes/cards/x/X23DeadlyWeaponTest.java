package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.d.DonatelloMutantMechanic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({X23DeadlyWeapon.class, DonatelloMutantMechanic.class, GrizzlyBears.class})
class X23DeadlyWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on an entering Mutant and X-23")
    void putsCountersOnEnteringMutantAndSource() {
        Permanent x23 = addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent mutant = harness.addToBattlefieldAndReturn(player1, new DonatelloMutantMechanic());
        resolveAllTriggers();

        assertThat(mutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(x23.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Mutant or an opponent's Mutant")
    void ignoresNonMutantsAndOpponentsMutants() {
        Permanent x23 = addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent nonMutant = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();
        Permanent opponentMutant = harness.addToBattlefieldAndReturn(player2, new DonatelloMutantMechanic());
        resolveAllTriggers();

        assertThat(nonMutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentMutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(x23.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when X-23 enters")
    void doesNotTriggerOnItsOwnEntry() {
        Permanent x23 = addCreatureReady(player1, new X23DeadlyWeapon());
        resolveAllTriggers();

        assertThat(x23.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
