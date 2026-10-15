package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.cards.r.RumblingBaloth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VastwoodHydra.class, DoomBlade.class, DeadlyRecluse.class, RumblingBaloth.class})
class VastwoodHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new VastwoodHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new RumblingBaloth());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());

        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 3);

        killHydra(hydra);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Death distribution puts counters only on controlled creatures")
    void deathDistributeIgnoresOpponentCreatures() {
        Permanent hydra = addCreatureReady(player1, new VastwoodHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        hydra.tap();
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        Permanent oppGiant = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());

        gd.pendingETBDamageAssignments = Map.of(ownBears.getId(), 3);

        killHydra(hydra);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(oppGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Accepting the death trigger allows choosing a distribution at resolution")
    void acceptingDeathTriggerPromptsForDistribution() {
        Permanent hydra = addCreatureReady(player1, new VastwoodHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player1, new DeadlyRecluse());
        harness.addToBattlefield(player1, new RumblingBaloth());

        killHydra(hydra);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Casting with X=0 dies and distributes no counters")
    void zeroXDiesAndDistributesNoCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        harness.setHand(player1, List.of(new VastwoodHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(VastwoodHydra.class::isInstance);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lethal minus counters do not reduce the death trigger's plus-counter snapshot")
    void lethalMinusCountersPreservePlusCounterCount() {
        Permanent hydra = addCreatureReady(player1, new VastwoodHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        hydra.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 2);

        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hydra);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void killHydra(Permanent hydra) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, hydra.getId());
    }
}
