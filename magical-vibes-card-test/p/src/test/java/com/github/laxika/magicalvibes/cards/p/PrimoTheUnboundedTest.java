package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HydroidKrasis;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimoTheUnbounded.class, GrizzlyBears.class, HydroidKrasis.class})
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
        resolveCombat();

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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    @Test
    @DisplayName("Casting with X zero puts Primo into the graveyard without counters")
    void diesWhenCastWithZeroX() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PrimoTheUnbounded()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primo, the Unbounded");
        harness.assertInGraveyard(player1, "Primo, the Unbounded");
    }

    @Test
    @DisplayName("Simultaneous qualifying attackers create one token using only their combined damage")
    void combinesQualifyingDamageAndExcludesNonzeroBasePowerDamage() {
        Permanent primo = addReadyPrimo(2);
        Permanent krasis = addCreatureReady(player1, new HydroidKrasis());
        krasis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        primo.setAttacking(true);
        krasis.setAttacking(true);
        bears.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.assertLife(player2, 13);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
    }

    @Test
    @DisplayName("Primo still triggers when it dies in the combat damage event")
    void triggersWhenPrimoDiesDuringCombatDamage() {
        addReadyPrimo(2);
        Permanent attacker = addCreatureReady(player1, new HydroidKrasis());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent blocker = addCreatureReady(player2, new HydroidKrasis());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));

        harness.assertInGraveyard(player1, "Primo, the Unbounded");
        harness.assertLife(player2, 17);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's base-power-zero creature does not trigger Primo")
    void ignoresOpponentsQualifyingCreature() {
        addReadyPrimo(1);
        Permanent attacker = addCreatureReady(player2, new HydroidKrasis());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        attacker.setAttacking(true);
        harness.setLife(player1, 20);

        resolveCombat(player2);
        harness.assertLife(player1, 17);
        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    private Permanent addReadyPrimo(int counters) {
        Permanent primo = addCreatureReady(player1, new PrimoTheUnbounded());
        primo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return primo;
    }
}
