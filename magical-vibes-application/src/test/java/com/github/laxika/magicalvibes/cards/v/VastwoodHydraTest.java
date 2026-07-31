package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VastwoodHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new VastwoodHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Vastwood Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hydra.getEffectivePower()).isEqualTo(3);
        assertThat(hydra.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("On death, may distribute +1/+1 counters among controlled creatures")
    void deathDistributesCountersAmongControlledCreatures() {
        Permanent hydra = addCreatureReady(player1, new VastwoodHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        hydra.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 2, giant.getId(), 1);

        killHydra(hydra);

        harness.passBothPriorities(); // death trigger resolves → "you may" prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death distribute is optional — declining places no counters")
    void deathDistributeCanBeDeclined() {
        Permanent hydra = addCreatureReady(player1, new VastwoodHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        hydra.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 3);

        killHydra(hydra);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Death distribute ignores opponent creatures in the assignment map")
    void deathDistributeIgnoresOpponentCreatures() {
        Permanent hydra = addCreatureReady(player1, new VastwoodHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        hydra.tap();
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oppGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        gd.pendingETBDamageAssignments = Map.of(ownBears.getId(), 1, oppGiant.getId(), 2);

        killHydra(hydra);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(oppGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void killHydra(Permanent hydra) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID hydraId = hydra.getId();
        gs.playCard(gd, player1, 0, 0, hydraId, null);
        harness.passBothPriorities(); // Assassinate resolves → hydra dies → death trigger on stack
    }
}
