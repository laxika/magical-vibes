package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.cards.i.InstillInfection;
import com.github.laxika.magicalvibes.cards.m.MindControl;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ProteanHydra.class, GrizzlyBears.class, Shock.class, InstillInfection.class,
        GiantGrowth.class, VampireHexmage.class, MindControl.class})
class ProteanHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWith3Counters() {
        harness.setHand(player1, List.of(new ProteanHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3); // 3 generic for X

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findHydra(player1);
        assertThat(hydra).isNotNull();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters as 0/0 and dies to state-based actions")
    void entersWith0CountersAndDies() {
        harness.setHand(player1, List.of(new ProteanHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA
        harness.assertNotOnBattlefield(player1, "Protean Hydra");
    }

    @Test
    @DisplayName("Shock damage is prevented and removes +1/+1 counters instead")
    void shockDamageRemovesCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new ProteanHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5); // 5/5

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hydraId = hydra.getId();
        harness.castInstant(player1, 0, hydraId);
        harness.passBothPriorities();

        // Hydra survives with 3 +1/+1 counters (5 - 2 from Shock damage)
        harness.assertOnBattlefield(player2, "Protean Hydra");
        Permanent survivingHydra = findHydra(player2);
        assertThat(survivingHydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage is prevented and removes +1/+1 counters")
    void combatDamageRemovesCounters() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ProteanHydra());
        blocker.setSummoningSick(false);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5); // 5/5
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Hydra survives with 3 +1/+1 counters (5 - 2 from Bears' power)
        Permanent survivingHydra = findHydra(player2);
        assertThat(survivingHydra).isNotNull();
        assertThat(survivingHydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage exceeding counter count only removes available counters, all damage still prevented")
    void damageExceedingCountersOnlyRemovesAvailable() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new ProteanHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 1/1

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hydraId = hydra.getId();
        harness.castInstant(player1, 0, hydraId);
        harness.passBothPriorities();

        // All damage is prevented, but only 1 counter can be removed (Shock deals 2)
        // With no counters remaining, its zero toughness causes it to die.
        harness.assertNotOnBattlefield(player2, "Protean Hydra");
    }

    @Test
    @DisplayName("When +1/+1 counters are removed, delayed trigger adds double counters at end step")
    void delayedRegrowthAtEndStep() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new ProteanHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5); // 5/5

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID hydraId = hydra.getId();
        harness.castInstant(player2, 0, hydraId);
        harness.passBothPriorities(); // Resolve Shock: 2 damage, remove 2 counters, 3 remaining

        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        // Advance to end step naturally (POSTCOMBAT_MAIN -> END_STEP triggers handler)
        advanceToEndStep(player1);
        // 2 individual triggers on stack (one per removed counter), each adds 2 counters
        resolveAllDelayedTriggers();

        // 2 counters removed → 2 triggers × 2 counters = 4 added, total = 3 + 4 = 7
        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Damage with no counters is still prevented and does not cause regrowth")
    void noTriggerWhenNoCountersToRemove() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new ProteanHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hydra.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();
        resolveAllDelayedTriggers();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();
        assertThat(hydra.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();

        advanceToEndStep(player1);
        resolveAllDelayedTriggers();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hydra regrows after combat damage")
    void regrowsAfterCombatDamage() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ProteanHydra());
        blocker.setSummoningSick(false);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5); // 5/5
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // Combat damage: Bears deal 2, remove 2 counters

        // Hydra has 3 counters after combat damage prevention
        Permanent survivingHydra = findHydra(player1);
        assertThat(survivingHydra).isNotNull();
        assertThat(survivingHydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        // Advance to end step naturally — delayed trigger fires
        advanceToEndStep(player2);
        resolveAllDelayedTriggers();

        // 2 counters removed → 4 counters added, total = 3 + 4 = 7
        assertThat(findHydra(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("-1/-1 counter on Hydra annihilates with +1/+1 counter via SBA, triggering regrowth")
    void minusOneCounterAnnihilationTriggersRegrowth() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new ProteanHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5); // 5/5

        // Add a card to player2's library so InstillInfection's draw doesn't lose the game
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        // Cast InstillInfection targeting Hydra (puts 1 -1/-1 counter + draws a card)
        harness.setHand(player2, List.of(new InstillInfection()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.WHITE, 3); // 3 generic

        UUID hydraId = hydra.getId();
        harness.castInstant(player2, 0, hydraId);
        harness.passBothPriorities(); // Resolves InstillInfection → SBA fires → counter annihilation

        // After SBA: 4 +1/+1 counters, 0 -1/-1 counters (1 pair annihilated)
        Permanent afterSba = findHydra(player1);
        assertThat(afterSba).isNotNull();
        assertThat(afterSba.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(afterSba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        // Advance to end step — delayed regrowth trigger fires
        advanceToEndStep(player1);
        resolveAllDelayedTriggers();

        // 1 counter removed via SBA → 1 trigger × 2 counters = 2 added, total = 4 + 2 = 6
        Permanent result = findHydra(player1);
        assertThat(result.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Each removed counter puts an immediate regrowth ability on the stack")
    void counterRemovalTriggersBeforeEndStep() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new ProteanHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        resolveAllDelayedTriggers();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Counters removed by Vampire Hexmage also regrow")
    void nonDamageCounterRemovalRegrows() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new ProteanHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hydra.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new VampireHexmage());
        harness.activateAbility(player2, 0, null, hydra.getId());
        harness.passBothPriorities();
        resolveAllDelayedTriggers();

        harness.assertOnBattlefield(player1, "Protean Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        advanceToEndStep(player1);
        resolveAllDelayedTriggers();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Regrowth retains its original controller when Hydra changes control")
    void delayedRegrowthRetainsController() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new ProteanHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player2, List.of(new Shock(), new MindControl()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();
        resolveAllDelayedTriggers();

        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, hydra.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Protean Hydra");
        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getControllerId()).isEqualTo(player1.getId()));
        resolveAllDelayedTriggers();
        assertThat(findHydra(player2).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    /**
     * Resolve all delayed triggers on the stack by repeatedly passing priorities
     * until the stack is empty.
     */
    private void resolveAllDelayedTriggers() {
        int safetyCounter = 0;
        while (!gd.stack.isEmpty() && safetyCounter < 20) {
            harness.passBothPriorities();
            safetyCounter++;
        }
    }

    private Permanent findHydra(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Protean Hydra"))
                .findFirst().orElse(null);
    }
}
