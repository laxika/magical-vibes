package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.cards.c.ContaminatedBond;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeadenMyr;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.cards.r.RelicPutrescence;
import com.github.laxika.magicalvibes.cards.s.SanguineBond;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ShrineOfBoundlessGrowth;
import com.github.laxika.magicalvibes.cards.v.ViridianRevel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for CR 603.3 compliance: triggered abilities from mana-ability sacrifices
 * are deferred to {@code pendingManaAbilityTriggers} and flushed onto the stack
 * at the correct time (after a spell/ability is placed on the stack, or when
 * priority is next passed).
 *
 * <p>Reproduces the fuzz-test bug where sacrificing Shrine of Boundless Growth
 * (a mana ability) triggered Viridian Revel, which went on the stack immediately
 * and blocked sorcery-speed spell casting.
 */
class ManaAbilityTriggerDeferralTest extends BaseCardTest {

    /**
     * Sets up the standard scenario:
     * <ul>
     *   <li>player1 controls Viridian Revel (triggers on opponent artifact → graveyard)</li>
     *   <li>player2 is active on PRECOMBAT_MAIN with a Shrine of Boundless Growth</li>
     * </ul>
     *
     * @param chargeCounters number of charge counters on the Shrine
     * @return the Shrine permanent (on player2's battlefield)
     */
    private Permanent setupShrineScenario(int chargeCounters) {
        // Player1 has Viridian Revel — triggers when opponent's artifact goes to graveyard
        harness.addToBattlefield(player1, new ViridianRevel());

        // Player2 is active player in main phase with the Shrine
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        ShrineOfBoundlessGrowth shrineCard = new ShrineOfBoundlessGrowth();
        Permanent shrine = new Permanent(shrineCard);
        shrine.setSummoningSick(false);
        shrine.setChargeCounters(chargeCounters);
        gd.playerBattlefields.get(player2.getId()).add(shrine);

        return shrine;
    }

    private Permanent addSecondShrine(Player player, int chargeCounters) {
        ShrineOfBoundlessGrowth card = new ShrineOfBoundlessGrowth();
        Permanent shrine = new Permanent(card);
        shrine.setSummoningSick(false);
        shrine.setChargeCounters(chargeCounters);
        gd.playerBattlefields.get(player.getId()).add(shrine);
        return shrine;
    }

    /**
     * Arms {@code player} with a playable instant so {@code resolveAutoPass}
     * stops instead of resolving the trigger and leaving us unable to inspect
     * the intermediate stack state. Needed because CR 117.5 / our engine now
     * auto-passes unresponsive players when the stack is non-empty.
     */
    private void armWithInstantResponse(Player player) {
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
    }

    // =========================================================================
    // Core bug fix: sorcery-speed spell cast after mana-ability sacrifice
    // =========================================================================

    @Nested
    @DisplayName("CR 603.3: Sorcery-speed spells castable after mana-ability sacrifice trigger")
    class SorcerySpeedCastAfterManaAbilitySacrifice {

        @Test
        @DisplayName("Creature spell castable after Shrine sacrifice triggers Viridian Revel")
        void creatureSpellCastableAfterShrineSacrifice() {
            setupShrineScenario(2);
            harness.addMana(player2, ManaColor.GREEN, 1);
            harness.setHand(player2, List.of(new GrizzlyBears())); // costs {1}{G}
            // Arm player1 with a response so auto-pass doesn't resolve the trigger.
            armWithInstantResponse(player1);

            // Activate Shrine mana ability — sacrifices an artifact, triggering Viridian Revel.
            // Shrine is the only permanent on player2's battlefield → index 0.
            harness.activateAbility(player2, 0, null, null);

            // Trigger should be deferred, not on the stack
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingManaAbilityTriggers).isNotEmpty();

            // Casting a sorcery-speed creature should succeed (stack appears empty)
            harness.castCreature(player2, 0);

            // Spell is on the stack, and deferred trigger flushed on top
            assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        }

        @Test
        @DisplayName("Sorcery spell castable after Shrine sacrifice triggers Viridian Revel")
        void sorcerySpellCastableAfterShrineSacrifice() {
            setupShrineScenario(3);
            harness.setHand(player2, List.of(new Divination())); // costs {2}{U}
            harness.addMana(player2, ManaColor.BLUE, 1);
            armWithInstantResponse(player1);

            // Activate Shrine (index 0) — 3 colorless + 1 blue = enough for {2}{U}
            harness.activateAbility(player2, 0, null, null);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingManaAbilityTriggers).isNotEmpty();

            // Sorcery-speed spell should succeed
            harness.castSorcery(player2, 0, 0);

            assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        }
    }

    // =========================================================================
    // Stack ordering: deferred triggers go ON TOP of the spell
    // =========================================================================

    @Nested
    @DisplayName("CR 603.3: Deferred triggers placed on top of spell")
    class StackOrdering {

        @Test
        @DisplayName("Viridian Revel trigger is on top of creature spell after cast")
        void triggerOnTopOfCreatureSpell() {
            setupShrineScenario(2);
            harness.addMana(player2, ManaColor.GREEN, 1);
            harness.setHand(player2, List.of(new GrizzlyBears()));
            armWithInstantResponse(player1);

            harness.activateAbility(player2, 0, null, null);
            harness.castCreature(player2, 0);

            // Bottom of stack: creature spell, top: triggered ability
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");

            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Viridian Revel");
        }

        @Test
        @DisplayName("Viridian Revel trigger is on top of sorcery spell after cast")
        void triggerOnTopOfSorcerySpell() {
            setupShrineScenario(3);
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.setHand(player2, List.of(new Divination()));
            armWithInstantResponse(player1);

            harness.activateAbility(player2, 0, null, null);
            harness.castSorcery(player2, 0, 0);

            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");

            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Viridian Revel");
        }
    }

    // =========================================================================
    // Priority pass: standalone mana ability triggers flush on passPriority
    // =========================================================================

    @Nested
    @DisplayName("Standalone mana ability: triggers flush when priority is passed")
    class FlushOnPriorityPass {

        @Test
        @DisplayName("Passing priority flushes pending mana-ability triggers to the stack")
        void passingPriorityFlushesPendingTriggers() {
            setupShrineScenario(1);
            // Arm player1 so auto-pass stops once she has priority to respond to the trigger.
            armWithInstantResponse(player1);

            // Activate Shrine mana ability (standalone, not paying for a spell)
            harness.activateAbility(player2, 0, null, null);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingManaAbilityTriggers).isNotEmpty();

            // Pass priority — triggers should flush
            harness.passPriority(player2);

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Viridian Revel");
            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        }

        @Test
        @DisplayName("Priority is cleared when pending triggers are flushed via passPriority")
        void priorityClearedOnFlush() {
            setupShrineScenario(1);
            // Arm player1 so after flush + auto-pass cascade, player1 is offered priority
            // (auto-pass bails on line 149 of resolveAutoPass) and priorityPassedBy keeps
            // the flush-cleared state we're asserting on.
            armWithInstantResponse(player1);

            // Player1 (non-active) already passed, player2 (active) hasn't
            gd.priorityPassedBy.clear();
            gd.priorityPassedBy.add(player1.getId());

            harness.activateAbility(player2, 0, null, null);

            // Pass priority — flush clears priority, then adds player2's pass
            gs.passPriority(gd, player2);

            // Priority was cleared (both passes reset) then player2 re-passed → size 1
            assertThat(gd.priorityPassedBy).hasSize(1);
            assertThat(gd.priorityPassedBy).contains(player2.getId());
        }
    }

    // =========================================================================
    // Instant-speed: mana-ability triggers don't block instant casting
    // =========================================================================

    @Nested
    @DisplayName("Instant-speed spells unaffected by pending triggers")
    class InstantSpeedUnaffected {

        @Test
        @DisplayName("Instant castable after Shrine sacrifice (stack-empty not required)")
        void instantCastableAfterShrineSacrifice() {
            setupShrineScenario(1);
            harness.addMana(player2, ManaColor.RED, 1);
            harness.setHand(player2, List.of(new Shock())); // costs {R}

            harness.activateAbility(player2, 0, null, null);

            // Cast instant targeting player1 — should succeed regardless
            harness.castInstant(player2, 0, player1.getId());

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        }
    }

    // =========================================================================
    // Multiple mana-ability sacrifices accumulate pending triggers
    // =========================================================================

    @Nested
    @DisplayName("Multiple mana-ability sacrifices accumulate pending triggers")
    class MultipleSacrifices {

        @Test
        @DisplayName("Two Shrine sacrifices produce two pending triggers, both flushed on spell cast")
        void twoShrinesBothTriggersFlushed() {
            // Player1 controls Viridian Revel
            harness.addToBattlefield(player1, new ViridianRevel());

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Two shrines on player2's battlefield
            Permanent shrine1 = addSecondShrine(player2, 1);
            Permanent shrine2 = addSecondShrine(player2, 2);

            harness.addMana(player2, ManaColor.GREEN, 1);
            harness.setHand(player2, List.of(new GrizzlyBears()));
            // Arm player1 so auto-pass bails on the top trigger instead of resolving.
            armWithInstantResponse(player1);

            // Activate first shrine (index 0)
            harness.activateAbility(player2, 0, null, null);
            assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

            // After sacrifice, second shrine moves to index 0
            harness.activateAbility(player2, 0, null, null);
            assertThat(gd.pendingManaAbilityTriggers).hasSize(2);

            // Stack should still be empty
            assertThat(gd.stack).isEmpty();

            // Cast creature — both triggers flush on top
            harness.castCreature(player2, 0);

            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            // Stack: creature spell (bottom), 2 Viridian Revel triggers (top)
            assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(3);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
            assertThat(gd.stack.stream()
                    .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                    .filter(e -> e.getCard().getName().equals("Viridian Revel"))
                    .count()).isEqualTo(2);
        }
    }

    // =========================================================================
    // AutoPass: pending triggers flushed before auto-pass can skip them
    // =========================================================================

    @Nested
    @DisplayName("AutoPass flushes pending triggers before skipping steps")
    class AutoPassFlush {

        @Test
        @DisplayName("Pending triggers are flushed to stack by resolveAutoPass")
        void resolveAutoPassFlushesPendingTriggers() {
            setupShrineScenario(1);
            // Arm player1 so the trigger sits on the stack for inspection rather than
            // resolving immediately via the auto-pass cascade.
            armWithInstantResponse(player1);

            // Activate Shrine — trigger goes to pending
            harness.activateAbility(player2, 0, null, null);
            assertThat(gd.pendingManaAbilityTriggers).isNotEmpty();

            // Manually place triggers on the pending list and invoke resolveAutoPass
            // by passing both priorities (passPriority flushes, then resolveAutoPass runs)
            harness.passPriority(player2);

            // Trigger should now be on the stack
            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Viridian Revel");
        }
    }

    // =========================================================================
    // Full resolution: trigger resolves correctly after being deferred
    // =========================================================================

    @Nested
    @DisplayName("Deferred triggers resolve correctly")
    class FullResolution {

        @Test
        @DisplayName("Viridian Revel trigger resolves and offers may-draw after spell cast")
        void deferredTriggerResolvesCorrectly() {
            setupShrineScenario(2);
            harness.addMana(player2, ManaColor.GREEN, 1);
            harness.setHand(player2, List.of(new GrizzlyBears()));

            harness.activateAbility(player2, 0, null, null);
            harness.castCreature(player2, 0);

            // Resolve the Viridian Revel trigger (top of stack) — should prompt player1
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

            // Accept the may draw
            int handSizeBefore = gd.playerHands.get(player1.getId()).size();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
        }

        @Test
        @DisplayName("Creature spell resolves after deferred trigger resolves")
        void creatureResolvesAfterDeferredTrigger() {
            setupShrineScenario(2);
            harness.addMana(player2, ManaColor.GREEN, 1);
            harness.setHand(player2, List.of(new GrizzlyBears()));

            harness.activateAbility(player2, 0, null, null);
            harness.castCreature(player2, 0);

            // Resolve Viridian Revel trigger first (it's on top)
            harness.passBothPriorities(); // trigger resolves → may prompt
            harness.handleMayAbilityChosen(player1, false); // decline draw

            // Now resolve the creature spell
            harness.passBothPriorities();

            // Grizzly Bears should be on the battlefield
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.stack).isEmpty();
        }
    }

    // =========================================================================
    // CR 603.2: basic mana tap (AbilityActivationService.tapPermanent) defers
    // enchanted-permanent-tap triggers (Relic Putrescence fuzz-bug repro)
    // =========================================================================

    @Nested
    @DisplayName("CR 603.2: basic-mana tap defers enchanted-permanent-tap triggers")
    class BasicManaTapTriggerDeferral {

        /**
         * Puts a Leaden Myr (artifact creature, {T}: add {B}) on {@code controller}'s
         * battlefield and attaches a Relic Putrescence controlled by the same player.
         * Forces active player and main phase so tapPermanent will succeed.
         */
        private Permanent setupEnchantedMyr(Player controller) {
            harness.addToBattlefield(controller, new LeadenMyr());
            Permanent myr = gd.playerBattlefields.get(controller.getId()).getFirst();
            myr.setSummoningSick(false);

            Permanent aura = new Permanent(new RelicPutrescence());
            aura.setAttachedTo(myr.getId());
            gd.playerBattlefields.get(controller.getId()).add(aura);

            harness.forceActivePlayer(controller);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            return myr;
        }

        @Test
        @DisplayName("Tapping enchanted artifact for mana defers Relic Putrescence trigger")
        void enchantedTapDefersTrigger() {
            setupEnchantedMyr(player2);

            harness.tapPermanent(player2, 0);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingManaAbilityTriggers).hasSize(1);
            assertThat(gd.pendingManaAbilityTriggers.getFirst().getCard().getName())
                    .isEqualTo("Relic Putrescence");
            assertThat(gd.pendingManaAbilityTriggers.getFirst().getEntryType())
                    .isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("Sorcery-speed aura castable after tap-trigger (fuzz bug repro)")
        void sorcerySpeedSpellCastableAfterTapTrigger() {
            Permanent myr = setupEnchantedMyr(player2);
            harness.addMana(player2, ManaColor.BLACK, 1); // plus {B} from tapping Leaden Myr
            harness.setHand(player2, List.of(new ContaminatedBond())); // {2}{B}, enchant creature
            // Arm player1 so auto-pass bails on the flushed trigger and we can inspect
            // the stack (Contaminated Bond on bottom, Relic Putrescence trigger on top).
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.tapPermanent(player2, 0); // tap Leaden Myr → Relic Putrescence triggers, deferred
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

            // Before the fix this would throw "Card is not playable" because stackEmpty=false.
            // (Contaminated Bond costs {2}{B}; we only have {1}{B}, but a second tap of the Myr
            //  is illegal — so bump the pool directly to isolate the stack-empty check.)
            harness.addMana(player2, ManaColor.BLACK, 1);
            harness.castEnchantment(player2, 0, myr.getId());

            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Contaminated Bond");
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Relic Putrescence");
        }

        @Test
        @DisplayName("Passing priority flushes a deferred tap-trigger to the stack")
        void passingPriorityFlushesDeferredTapTrigger() {
            setupEnchantedMyr(player2);
            // Arm player1 so auto-pass bails once the trigger is on the stack.
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.tapPermanent(player2, 0);
            assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

            harness.passPriority(player2);

            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Relic Putrescence");
        }
    }

    // =========================================================================
    // CR 603.2: life-gain from a mana ability (Pristine Talisman) defers
    // the Sanguine Bond trigger, so a sorcery-speed spell is still castable
    // on the same priority window (fuzz-log repro).
    // =========================================================================

    @Nested
    @DisplayName("CR 603.2: life-gain from a mana ability defers Sanguine Bond trigger")
    class LifeGainFromManaAbility {

        /**
         * Puts Pristine Talisman (index 0) and Sanguine Bond (index 1) on {@code controller}'s
         * battlefield, both untapped and not summoning sick. Forces main phase and active player.
         */
        private Permanent setupTalismanAndSanguineBond(Player controller) {
            Permanent talisman = new Permanent(new PristineTalisman());
            talisman.setSummoningSick(false);
            gd.playerBattlefields.get(controller.getId()).add(talisman);

            Permanent bond = new Permanent(new SanguineBond());
            bond.setSummoningSick(false);
            gd.playerBattlefields.get(controller.getId()).add(bond);

            harness.forceActivePlayer(controller);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            return talisman;
        }

        @Test
        @DisplayName("Sorcery castable after Pristine Talisman life gain triggers Sanguine Bond")
        void sorceryCastableAfterSanguineBondTrigger() {
            setupTalismanAndSanguineBond(player2);
            harness.setHand(player2, List.of(new Divination())); // {2}{U}
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 2);
            armWithInstantResponse(player1);

            // Activate Pristine Talisman (index 0) — adds {C} + 1 life → Sanguine Bond triggers.
            harness.activateAbility(player2, 0, null, null);

            // Before the fix, the Sanguine Bond trigger went straight to the stack,
            // blocking the sorcery cast. Now it must sit in the deferred queue.
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingManaAbilityTriggers).hasSize(1);
            assertThat(gd.pendingManaAbilityTriggers.getFirst().getCard().getName())
                    .isEqualTo("Sanguine Bond");

            // Sorcery-speed cast on the same priority window must succeed.
            harness.castSorcery(player2, 0, 0);

            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Sanguine Bond");
        }

        @Test
        @DisplayName("Passing priority flushes the deferred Sanguine Bond trigger onto the stack")
        void passingPriorityFlushesSanguineBondTrigger() {
            setupTalismanAndSanguineBond(player2);
            armWithInstantResponse(player1);

            harness.activateAbility(player2, 0, null, null);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

            harness.passPriority(player2);

            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sanguine Bond");
        }
    }

    // =========================================================================
    // No-trigger case: mana ability without opponent trigger works normally
    // =========================================================================

    @Nested
    @DisplayName("Mana ability without triggers still works normally")
    class NoTriggerCase {

        @Test
        @DisplayName("Shrine sacrifice without Viridian Revel leaves no pending triggers")
        void shrineWithoutViridianRevelNoPendingTriggers() {
            // No Viridian Revel — just a Shrine
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            ShrineOfBoundlessGrowth shrineCard = new ShrineOfBoundlessGrowth();
            Permanent shrine = new Permanent(shrineCard);
            shrine.setSummoningSick(false);
            shrine.setChargeCounters(2);
            gd.playerBattlefields.get(player2.getId()).add(shrine);

            harness.addMana(player2, ManaColor.GREEN, 1);
            harness.setHand(player2, List.of(new GrizzlyBears()));
            // Arm player1 so auto-pass bails when the spell is on the stack.
            armWithInstantResponse(player1);

            harness.activateAbility(player2, 0, null, null);

            // No triggers at all
            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingManaAbilityTriggers).isEmpty();

            // Spell cast works normally
            harness.castCreature(player2, 0);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        }
    }
}
