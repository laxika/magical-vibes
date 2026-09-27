package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PrimoTheUnbounded.class, GrizzlyBears.class})
class PrimoTheUnboundedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with twice the announced X in +1/+1 counters")
    void entersWithTwiceXPlusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PrimoTheUnbounded()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent primo = findPermanent(player1, "Primo, the Unbounded");
        assertThat(primo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Base-power-zero combat damage creates a Fractal with counters equal to damage")
    void createsFractalWithCombatDamageCounters() {
        Permanent primo = addReadyPrimo(2);
        primo.setAttacking(true);
        harness.setLife(player2, 20);
        goToCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature with nonzero base power does not satisfy the combat trigger")
    void ignoresCreatureWithNonzeroBasePower() {
        addReadyPrimo(1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.setLife(player2, 20);
        goToCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    private Permanent addReadyPrimo(int counters) {
        Permanent primo = harness.addToBattlefieldAndReturn(player1, new PrimoTheUnbounded());
        primo.setSummoningSick(false);
        primo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return primo;
    }

    private void goToCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
