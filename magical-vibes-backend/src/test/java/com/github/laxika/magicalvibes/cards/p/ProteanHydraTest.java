package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InstillInfection;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXPlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProteanHydraTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has EnterWithXPlusOnePlusOneCountersEffect as ETB effect")
    void hasCorrectETBEffect() {
        ProteanHydra card = new ProteanHydra();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithXPlusOnePlusOneCountersEffect.class);
    }

    @Test
    @DisplayName("Has PreventDamageAndRemovePlusOnePlusOneCountersEffect and DelayedPlusOnePlusOneCounterRegrowthEffect as static effects")
    void hasCorrectStaticEffects() {
        ProteanHydra card = new ProteanHydra();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof PreventDamageAndRemovePlusOnePlusOneCountersEffect);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof DelayedPlusOnePlusOneCounterRegrowthEffect);
    }

    // ===== Enters with X +1/+1 counters =====

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
        assertThat(hydra.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters as 0/0 and dies to state-based actions")
    void entersWith0CountersAndDies() {
        harness.setHand(player1, List.of(new ProteanHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Protean Hydra"));
    }

    // ===== Damage prevention removes +1/+1 counters =====

    @Test
    @DisplayName("Shock damage is prevented and removes +1/+1 counters instead")
    void shockDamageRemovesCounters() {
        harness.addToBattlefield(player2, new ProteanHydra());
        Permanent hydra = findHydra(player2);
        hydra.setPlusOnePlusOneCounters(5); // 5/5

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hydraId = hydra.getId();
        harness.castInstant(player1, 0, hydraId);
        harness.passBothPriorities();

        // Hydra survives with 3 +1/+1 counters (5 - 2 from Shock damage)
        harness.assertOnBattlefield(player2, "Protean Hydra");
        Permanent survivingHydra = findHydra(player2);
        assertThat(survivingHydra.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage is prevented and removes +1/+1 counters")
    void combatDamageRemovesCounters() {
        ProteanHydra hydraCard = new ProteanHydra();
        Permanent blocker = new Permanent(hydraCard);
        blocker.setSummoningSick(false);
        blocker.setPlusOnePlusOneCounters(5); // 5/5
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Hydra survives with 3 +1/+1 counters (5 - 2 from Bears' power)
        Permanent survivingHydra = findHydra(player2);
        assertThat(survivingHydra).isNotNull();
        assertThat(survivingHydra.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    // ===== Damage beyond counter count =====

    @Test
    @DisplayName("Damage exceeding counter count only removes available counters, all damage still prevented")
    void damageExceedingCountersOnlyRemovesAvailable() {
        harness.addToBattlefield(player2, new ProteanHydra());
        Permanent hydra = findHydra(player2);
        hydra.setPlusOnePlusOneCounters(1); // 1/1

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hydraId = hydra.getId();
        harness.castInstant(player1, 0, hydraId);
        harness.passBothPriorities();

        // All damage is prevented, but only 1 counter can be removed (Shock deals 2)
        // Hydra survives as 0/0 but should die to state-based actions
        // Actually, with 0 +1/+1 counters it's 0/0 and dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Protean Hydra"));
    }

    // ===== Delayed regrowth trigger =====

    @Test
    @DisplayName("When +1/+1 counters are removed, delayed trigger adds double counters at end step")
    void delayedRegrowthAtEndStep() {
        harness.addToBattlefield(player1, new ProteanHydra());
        Permanent hydra = findHydra(player1);
        hydra.setPlusOnePlusOneCounters(5); // 5/5

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID hydraId = hydra.getId();
        harness.castInstant(player2, 0, hydraId);
        harness.passBothPriorities(); // Resolve Shock: 2 damage, remove 2 counters, 3 remaining

        assertThat(findHydra(player1).getPlusOnePlusOneCounters()).isEqualTo(3);

        // Advance to end step naturally (POSTCOMBAT_MAIN -> END_STEP triggers handler)
        advanceToEndStep(player1);
        // 2 individual triggers on stack (one per removed counter), each adds 2 counters
        resolveAllDelayedTriggers();

        // 2 counters removed → 2 triggers × 2 counters = 4 added, total = 3 + 4 = 7
        assertThat(findHydra(player1).getPlusOnePlusOneCounters()).isEqualTo(7);
    }

    @Test
    @DisplayName("No delayed trigger when hydra has no counters to remove")
    void noTriggerWhenNoCountersToRemove() {
        harness.addToBattlefield(player1, new ProteanHydra());
        Permanent hydra = findHydra(player1);
        hydra.setPlusOnePlusOneCounters(0); // 0/0 but keep alive manually

        // Verify no pending delayed counters
        assertThat(gd.pendingDelayedPlusOnePlusOneCounters).isEmpty();
    }

    @Test
    @DisplayName("Hydra regrows after combat damage")
    void regrowsAfterCombatDamage() {
        ProteanHydra hydraCard = new ProteanHydra();
        Permanent blocker = new Permanent(hydraCard);
        blocker.setSummoningSick(false);
        blocker.setPlusOnePlusOneCounters(5); // 5/5
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(blocker);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // Combat damage: Bears deal 2, remove 2 counters

        // Hydra has 3 counters after combat damage prevention
        Permanent survivingHydra = findHydra(player1);
        assertThat(survivingHydra).isNotNull();
        assertThat(survivingHydra.getPlusOnePlusOneCounters()).isEqualTo(3);

        // Advance to end step naturally — delayed trigger fires
        advanceToEndStep(player2);
        resolveAllDelayedTriggers();

        // 2 counters removed → 4 counters added, total = 3 + 4 = 7
        assertThat(findHydra(player1).getPlusOnePlusOneCounters()).isEqualTo(7);
    }

    // ===== SBA counter annihilation triggers regrowth (ruling #3, #5) =====

    @Test
    @DisplayName("-1/-1 counter on Hydra annihilates with +1/+1 counter via SBA, triggering regrowth")
    void minusOneCounterAnnihilationTriggersRegrowth() {
        harness.addToBattlefield(player1, new ProteanHydra());
        Permanent hydra = findHydra(player1);
        hydra.setPlusOnePlusOneCounters(5); // 5/5

        // Add a card to player2's library so InstillInfection's draw doesn't lose the game
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

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
        assertThat(afterSba.getMinusOneMinusOneCounters()).isEqualTo(0);
        assertThat(afterSba.getPlusOnePlusOneCounters()).isEqualTo(4);

        // Advance to end step — delayed regrowth trigger fires
        advanceToEndStep(player1);
        resolveAllDelayedTriggers();

        // 1 counter removed via SBA → 1 trigger × 2 counters = 2 added, total = 4 + 2 = 6
        Permanent result = findHydra(player1);
        assertThat(result.getPlusOnePlusOneCounters()).isEqualTo(6);
    }

    // ===== Helpers =====

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances POSTCOMBAT_MAIN -> END_STEP, fires handler
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
