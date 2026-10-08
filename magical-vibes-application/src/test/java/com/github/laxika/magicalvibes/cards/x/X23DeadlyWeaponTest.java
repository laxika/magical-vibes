package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.c.ColossusSteelStalwart;
import com.github.laxika.magicalvibes.cards.l.LukeCageHeroForHire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({X23DeadlyWeapon.class, ColossusSteelStalwart.class, LukeCageHeroForHire.class})
class X23DeadlyWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on an entering Mutant and X-23")
    void putsCountersOnEnteringMutantAndSource() {
        Permanent x23 = addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent mutant = harness.enterBattlefieldAndReturn(player1, new ColossusSteelStalwart());
        resolveAllTriggers();

        assertThat(mutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(x23.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Mutant or an opponent's Mutant")
    void ignoresNonMutantsAndOpponentsMutants() {
        Permanent x23 = addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent nonMutant = harness.enterBattlefieldAndReturn(player1, new LukeCageHeroForHire());
        resolveAllTriggers();
        Permanent opponentMutant = harness.enterBattlefieldAndReturn(player2, new ColossusSteelStalwart());
        resolveAllTriggers();

        assertThat(nonMutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentMutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(x23.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when X-23 enters")
    void doesNotTriggerOnItsOwnEntry() {
        Permanent x23 = harness.enterBattlefieldAndReturn(player1, new X23DeadlyWeapon());
        resolveAllTriggers();

        assertThat(x23.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still puts a counter on X-23 if the entering Mutant leaves before resolution")
    void enteringMutantLeavingDoesNotPreventSourceCounter() {
        Permanent x23 = addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent mutant = harness.enterBattlefieldAndReturn(player1, new ColossusSteelStalwart());
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, mutant));
        resolveAllTriggers();

        assertThat(x23.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Still puts a counter on the entering Mutant if X-23 leaves before resolution")
    void sourceLeavingDoesNotPreventEnteringMutantCounter() {
        Permanent x23 = addCreatureReady(player1, new X23DeadlyWeapon());
        Permanent mutant = harness.enterBattlefieldAndReturn(player1, new ColossusSteelStalwart());
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, x23));
        resolveAllTriggers();

        assertThat(mutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(x23.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
