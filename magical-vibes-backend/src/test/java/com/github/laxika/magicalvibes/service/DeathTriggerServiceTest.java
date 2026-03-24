package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentLeavesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeathTriggerServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    private DeathTriggerService svc;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        svc = new DeathTriggerService(gameQueryService, gameBroadcastService);
        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER2_ID, new ArrayList<>());
    }

    /** Stubs cardHasSubtype to delegate to real implementation (for tests with SubtypeConditionalEffect). */
    private void stubSubtypeChecks() {
        when(gameQueryService.cardHasSubtype(any(), any(), any(), any())).thenCallRealMethod();
    }

    /** Stubs both cardHasSubtype and the granted subtypes lookup (for tests where subtype is NOT on the card). */
    private void stubSubtypeChecksWithGrantedLookup() {
        stubSubtypeChecks();
        when(gameQueryService.computeGrantedSubtypesForOwnedCreatureCard(any(), any())).thenReturn(List.of());
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Card createEquipment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        card.setManaCost("");
        return card;
    }

    private Card createEnchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("");
        return card;
    }

    private Card createArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("");
        return card;
    }

    private Permanent addToBattlefield(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    // ==================== collectDeathTrigger ====================

    @Nested
    @DisplayName("collectDeathTrigger")
    class CollectDeathTrigger {

        @Test
        @DisplayName("Does nothing when card has no ON_DEATH effects")
        void noEffectsDoesNothing() {
            Card card = createCreature("Vanilla", 2, 2);

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).isEmpty();
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Non-targeting effect adds triggered ability to stack")
        void nonTargetingEffectAddsToStack() {
            Card card = createCreature("Dying Dude", 2, 2);
            card.addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(1));

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(card);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getDescription()).isEqualTo("Dying Dude's ability");
            assertThat(entry.getEffectsToResolve()).hasSize(1);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("Permanent-targeting effect queues DeathTriggerTarget")
        void permanentTargetingEffectQueuesDeathTriggerTarget() {
            Card card = createCreature("Targeting Dude", 3, 3);
            card.addEffect(EffectSlot.ON_DEATH, new PutChargeCounterOnTargetPermanentEffect());

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
            PermanentChoiceContext.DeathTriggerTarget target = gd.pendingDeathTriggerTargets.peek();
            assertThat(target.dyingCard()).isEqualTo(card);
            assertThat(target.controllerId()).isEqualTo(PLAYER1_ID);
            assertThat(target.effects()).hasSize(1);
            assertThat(target.effects().get(0)).isInstanceOf(PutChargeCounterOnTargetPermanentEffect.class);
        }

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card card = createCreature("Optional Dude", 1, 1);
            MayEffect may = new MayEffect(new DrawCardEffect(1), "Draw a card?");
            card.addEffect(EffectSlot.ON_DEATH, may);

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true);

            // CR 603.5 — "you may" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getCard()).isEqualTo(card);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("MayPayManaEffect queues may ability with mana cost")
        void mayPayManaEffectQueuesMayAbility() {
            Card card = createCreature("Mana Dude", 2, 2);
            MayPayManaEffect mayPay = new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay 2 to draw?");
            card.addEffect(EffectSlot.ON_DEATH, mayPay);

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true);

            // CR 603.5 — "you may pay" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getCard()).isEqualTo(card);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(MayPayManaEffect.class);
        }

        @Test
        @DisplayName("TargetPlayerLosesLifeEqualToPowerEffect bakes power into concrete effect")
        void losesLifeEqualToPowerBakesPower() {
            Card card = createCreature("Vengeful", 4, 3);
            card.addEffect(EffectSlot.ON_DEATH, new TargetPlayerLosesLifeEqualToPowerEffect());
            Permanent perm = new Permanent(card);

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
            PermanentChoiceContext.DeathTriggerTarget target = gd.pendingDeathTriggerTargets.peek();
            assertThat(target.effects()).hasSize(1);
            assertThat(target.effects().get(0)).isInstanceOf(TargetPlayerLosesLifeEffect.class);
            TargetPlayerLosesLifeEffect resolved = (TargetPlayerLosesLifeEffect) target.effects().get(0);
            assertThat(resolved.amount()).isEqualTo(4);
        }

        @Test
        @DisplayName("TargetPlayerLosesLifeEqualToPowerEffect falls back to card power when no permanent")
        void losesLifeEqualToPowerFallsBackToCardPower() {
            Card card = createCreature("Vengeful Ghost", 3, 2);
            card.addEffect(EffectSlot.ON_DEATH, new TargetPlayerLosesLifeEqualToPowerEffect());

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, null);

            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
            TargetPlayerLosesLifeEffect resolved =
                    (TargetPlayerLosesLifeEffect) gd.pendingDeathTriggerTargets.peek().effects().get(0);
            assertThat(resolved.amount()).isEqualTo(3);
        }

        @Test
        @DisplayName("TargetPlayerLosesLifeEqualToPowerEffect clamps negative power to 0")
        void losesLifeEqualToPowerClampsNegativePowerToZero() {
            Card card = createCreature("Weakened", 2, 2);
            card.addEffect(EffectSlot.ON_DEATH, new TargetPlayerLosesLifeEqualToPowerEffect());
            Permanent perm = new Permanent(card);
            perm.setPowerModifier(-5);

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

            TargetPlayerLosesLifeEffect resolved =
                    (TargetPlayerLosesLifeEffect) gd.pendingDeathTriggerTargets.peek().effects().get(0);
            assertThat(resolved.amount()).isEqualTo(0);
        }

        @Test
        @DisplayName("TargetPlayerLosesLifeEqualToPowerEffect falls back to 0 when card power is null and no permanent")
        void losesLifeEqualToPowerNullCardPowerNoPermanentResolvesToZero() {
            Card card = createCreature("Powerless", 0, 2);
            card.setPower(null);
            card.addEffect(EffectSlot.ON_DEATH, new TargetPlayerLosesLifeEqualToPowerEffect());

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, null);

            TargetPlayerLosesLifeEffect resolved =
                    (TargetPlayerLosesLifeEffect) gd.pendingDeathTriggerTargets.peek().effects().get(0);
            assertThat(resolved.amount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Targeted MayEffect goes to pendingDeathTriggerTargets, not may queue (CR 603.3d)")
        void targetedMayEffectGoesToPendingTargets() {
            Card card = createCreature("Targeted May Dude", 2, 2);
            MayEffect may = new MayEffect(new TargetPlayerLosesLifeEffect(3), "Drain target player?");
            card.addEffect(EffectSlot.ON_DEATH, may);

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true);

            // CR 603.3d: targeted "may" abilities need the target chosen when stacking
            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
            PermanentChoiceContext.DeathTriggerTarget target = gd.pendingDeathTriggerTargets.peek();
            assertThat(target.effects().get(0)).isInstanceOf(MayEffect.class);
            assertThat(gd.stack).isEmpty();
        }

        @Nested
        @DisplayName("DealDamageToBlockedAttackersOnDeathEffect")
        class BlockedAttackersDamage {

            @Test
            @DisplayName("Triggers during combat when creature was blocking")
            void triggersDuringCombat() {
                Card card = createCreature("Thorny Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                Permanent perm = new Permanent(card);
                UUID attackerId = UUID.randomUUID();
                perm.getBlockingTargetIds().add(attackerId);
                gd.currentStep = TurnStep.COMBAT_DAMAGE;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

                assertThat(gd.stack).hasSize(1);
                StackEntry entry = gd.stack.get(0);
                assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DealDamageToBlockedAttackersOnDeathEffect.class);
                assertThat(entry.getTargetIds()).containsExactly(attackerId);
            }

            @Test
            @DisplayName("Does not trigger outside combat")
            void doesNotTriggerOutsideCombat() {
                Card card = createCreature("Thorny Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                Permanent perm = new Permanent(card);
                perm.getBlockingTargetIds().add(UUID.randomUUID());
                gd.currentStep = TurnStep.PRECOMBAT_MAIN;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

                assertThat(gd.stack).isEmpty();
            }

            @Test
            @DisplayName("Does not trigger when not blocking")
            void doesNotTriggerWhenNotBlocking() {
                Card card = createCreature("Non-Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                Permanent perm = new Permanent(card);
                gd.currentStep = TurnStep.COMBAT_DAMAGE;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

                assertThat(gd.stack).isEmpty();
            }

            @Test
            @DisplayName("Does not trigger when dyingPermanent is null")
            void doesNotTriggerWhenPermanentNull() {
                Card card = createCreature("Ghost Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                gd.currentStep = TurnStep.COMBAT_DAMAGE;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, null);

                assertThat(gd.stack).isEmpty();
            }

            @Test
            @DisplayName("Does not trigger when step is null")
            void doesNotTriggerWhenStepNull() {
                Card card = createCreature("Null Step Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                Permanent perm = new Permanent(card);
                perm.getBlockingTargetIds().add(UUID.randomUUID());
                gd.currentStep = null;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

                assertThat(gd.stack).isEmpty();
            }

            @Test
            @DisplayName("Triggers at BEGINNING_OF_COMBAT step")
            void triggersAtBeginningOfCombat() {
                Card card = createCreature("Thorny Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                Permanent perm = new Permanent(card);
                perm.getBlockingTargetIds().add(UUID.randomUUID());
                gd.currentStep = TurnStep.BEGINNING_OF_COMBAT;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

                assertThat(gd.stack).hasSize(1);
            }

            @Test
            @DisplayName("Triggers at END_OF_COMBAT step")
            void triggersAtEndOfCombat() {
                Card card = createCreature("Thorny Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                Permanent perm = new Permanent(card);
                perm.getBlockingTargetIds().add(UUID.randomUUID());
                gd.currentStep = TurnStep.END_OF_COMBAT;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

                assertThat(gd.stack).hasSize(1);
            }
        }

        @Test
        @DisplayName("Multiple effects on one card all get processed")
        void multipleEffectsAllProcessed() {
            Card card = createCreature("Multi-Trigger", 2, 2);
            card.addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(1));
            card.addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(2));

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true);

            assertThat(gd.stack).hasSize(2);
        }
    }

    // ==================== triggerDelayedPoisonOnDeath ====================

    @Nested
    @DisplayName("triggerDelayedPoisonOnDeath")
    class TriggerDelayedPoison {

        @Test
        @DisplayName("Applies poison counters from delayed trigger")
        void appliesPoisonCounters() {
            UUID cardId = UUID.randomUUID();
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(cardId, 3);
            when(gameQueryService.canPlayerGetPoisonCounters(gd, PLAYER1_ID)).thenReturn(true);

            svc.triggerDelayedPoisonOnDeath(gd, cardId, PLAYER1_ID);

            assertThat(gd.playerPoisonCounters.get(PLAYER1_ID)).isEqualTo(3);
        }

        @Test
        @DisplayName("Does nothing when no delayed trigger for this card")
        void noDelayedTriggerDoesNothing() {
            svc.triggerDelayedPoisonOnDeath(gd, UUID.randomUUID(), PLAYER1_ID);

            assertThat(gd.playerPoisonCounters.get(PLAYER1_ID)).isNull();
        }

        @Test
        @DisplayName("Does nothing when poison amount is zero")
        void zeroPoisonAmountDoesNothing() {
            UUID cardId = UUID.randomUUID();
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(cardId, 0);

            svc.triggerDelayedPoisonOnDeath(gd, cardId, PLAYER1_ID);

            assertThat(gd.playerPoisonCounters.get(PLAYER1_ID)).isNull();
        }

        @Test
        @DisplayName("Does nothing when poison amount is negative")
        void negativePoisonAmountDoesNothing() {
            UUID cardId = UUID.randomUUID();
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(cardId, -1);

            svc.triggerDelayedPoisonOnDeath(gd, cardId, PLAYER1_ID);

            assertThat(gd.playerPoisonCounters.get(PLAYER1_ID)).isNull();
        }

        @Test
        @DisplayName("Stacks on top of existing poison counters")
        void stacksOnExisting() {
            UUID cardId = UUID.randomUUID();
            gd.playerPoisonCounters.put(PLAYER1_ID, 2);
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(cardId, 3);
            when(gameQueryService.canPlayerGetPoisonCounters(gd, PLAYER1_ID)).thenReturn(true);

            svc.triggerDelayedPoisonOnDeath(gd, cardId, PLAYER1_ID);

            assertThat(gd.playerPoisonCounters.get(PLAYER1_ID)).isEqualTo(5);
        }

        @Test
        @DisplayName("Removes the delayed trigger entry after processing")
        void removesEntry() {
            UUID cardId = UUID.randomUUID();
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(cardId, 1);
            when(gameQueryService.canPlayerGetPoisonCounters(gd, PLAYER1_ID)).thenReturn(true);

            svc.triggerDelayedPoisonOnDeath(gd, cardId, PLAYER1_ID);

            assertThat(gd.creatureGivingControllerPoisonOnDeathThisTurn).doesNotContainKey(cardId);
        }

        @Test
        @DisplayName("Does not apply poison when player cannot get poison counters")
        void cannotGetPoisonCountersDoesNothing() {
            UUID cardId = UUID.randomUUID();
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(cardId, 3);
            when(gameQueryService.canPlayerGetPoisonCounters(gd, PLAYER1_ID)).thenReturn(false);

            svc.triggerDelayedPoisonOnDeath(gd, cardId, PLAYER1_ID);

            assertThat(gd.playerPoisonCounters.get(PLAYER1_ID)).isNull();
            verifyNoInteractions(gameBroadcastService);
        }

        @Test
        @DisplayName("Adds game log entry")
        void addsGameLog() {
            UUID cardId = UUID.randomUUID();
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(cardId, 2);
            when(gameQueryService.canPlayerGetPoisonCounters(gd, PLAYER1_ID)).thenReturn(true);

            svc.triggerDelayedPoisonOnDeath(gd, cardId, PLAYER1_ID);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("poison counter")));
        }
    }

    // ==================== checkAllyCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkAllyCreatureDeathTriggers")
    class AllyCreatureDeathTriggers {

        private final Card dyingCreature = createCreature("Dying Creature", 2, 2);

        @Test
        @DisplayName("Does nothing when battlefield is null")
        void nullBattlefieldDoesNothing() {
            gd.playerBattlefields.remove(PLAYER1_ID);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingCreature);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when no permanents have ON_ALLY_CREATURE_DIES effects")
        void noEffectsDoesNothing() {
            addToBattlefield(PLAYER1_ID, createCreature("Vanilla", 2, 2));

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingCreature);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Adds triggered ability to stack for non-may effect with sourcePermanentId")
        void nonMayEffectAddsToStack() {
            Card watcher = createCreature("Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DrawCardEffect(1));
            Permanent watcherPerm = addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingCreature);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(watcher);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getSourcePermanentId()).isEqualTo(watcherPerm.getId());
        }

        @Test
        @DisplayName("Queues may ability for MayEffect with sourcePermanentId")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("May Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new MayEffect(new DrawCardEffect(1), "Draw?"));
            Permanent watcherPerm = addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingCreature);

            // CR 603.5 — "you may" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(watcherPerm.getId());
        }

        @Test
        @DisplayName("Queues may ability for MayPayManaEffect")
        void mayPayManaEffectQueuesMayAbility() {
            Card watcher = createCreature("Pay Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new MayPayManaEffect("{1}", new DrawCardEffect(1), "Pay to draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingCreature);

            // CR 603.5 — "you may pay" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayPayManaEffect.class);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card watcher = createCreature("Blood Artist", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingCreature);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Blood Artist") && msg.contains("triggers")));
        }

        @Test
        @DisplayName("Only triggers for permanents controlled by dying creature's controller")
        void onlyOwnBattlefield() {
            Card watcher = createCreature("Enemy Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER2_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingCreature);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("SubtypeConditionalEffect fires when dying creature has matching subtype")
        void subtypeConditionalMatchingSubtypeFires() {
            stubSubtypeChecks();
            Card watcher = createCreature("Slimefoot", 2, 3);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                    new SubtypeConditionalEffect(CardSubtype.SAPROLING, new DrawCardEffect(1)));
            addToBattlefield(PLAYER1_ID, watcher);

            Card dyingSaproling = createCreature("Saproling", 1, 1);
            dyingSaproling.setSubtypes(List.of(CardSubtype.SAPROLING));

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingSaproling);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("SubtypeConditionalEffect does NOT fire when dying creature lacks subtype")
        void subtypeConditionalNonMatchingSubtypeDoesNotFire() {
            stubSubtypeChecksWithGrantedLookup();
            Card watcher = createCreature("Slimefoot", 2, 3);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                    new SubtypeConditionalEffect(CardSubtype.SAPROLING, new DrawCardEffect(1)));
            addToBattlefield(PLAYER1_ID, watcher);

            Card dyingBear = createCreature("Grizzly Bears", 2, 2);
            dyingBear.setSubtypes(List.of(CardSubtype.BEAR));

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingBear);

            assertThat(gd.stack).isEmpty();
            verifyNoInteractions(gameBroadcastService);
        }

        @Test
        @DisplayName("Mixed SubtypeConditional — only matching effect fires, non-matching is skipped")
        void subtypeConditionalMixedMatchOnlyMatchingFires() {
            stubSubtypeChecksWithGrantedLookup();
            Card watcher = createCreature("Mixed Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                    new SubtypeConditionalEffect(CardSubtype.SAPROLING, new DrawCardEffect(1)));
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                    new SubtypeConditionalEffect(CardSubtype.ZOMBIE, new GainLifeEffect(2)));
            addToBattlefield(PLAYER1_ID, watcher);

            Card dyingSaproling = createCreature("Saproling", 1, 1);
            dyingSaproling.setSubtypes(List.of(CardSubtype.SAPROLING));

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingSaproling);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEffectsToResolve()).hasSize(1);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("Multiple effects grouped into single stack entry for same permanent")
        void multipleEffectsGroupedInSingleStackEntry() {
            stubSubtypeChecks();
            Card watcher = createCreature("Multi Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                    new SubtypeConditionalEffect(CardSubtype.SAPROLING, new DealDamageToEachOpponentEffect(1)));
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                    new SubtypeConditionalEffect(CardSubtype.SAPROLING, new GainLifeEffect(1)));
            addToBattlefield(PLAYER1_ID, watcher);

            Card dyingSaproling = createCreature("Saproling", 1, 1);
            dyingSaproling.setSubtypes(List.of(CardSubtype.SAPROLING));

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID, dyingSaproling);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEffectsToResolve()).hasSize(2);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DealDamageToEachOpponentEffect.class);
            assertThat(entry.getEffectsToResolve().get(1)).isInstanceOf(GainLifeEffect.class);
        }
    }

    // ==================== checkEquippedCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkEquippedCreatureDeathTriggers")
    class EquippedCreatureDeathTriggers {

        @Test
        @DisplayName("Does nothing when battlefield is null")
        void nullBattlefieldDoesNothing() {
            gd.playerBattlefields.remove(PLAYER1_ID);

            svc.checkEquippedCreatureDeathTriggers(gd, UUID.randomUUID(), PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Triggers for equipment attached to dying creature")
        void triggersForAttachedEquipment() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card equipment = createEquipment("Death Sword");
            equipment.addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new DrawCardEffect(1));
            Permanent equipPerm = new Permanent(equipment);
            equipPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(equipPerm);

            svc.checkEquippedCreatureDeathTriggers(gd, creaturePerm.getId(), PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getCard()).isEqualTo(equipment);
            assertThat(entry.getDescription()).contains("Death Sword");
        }

        @Test
        @DisplayName("Does not trigger for equipment attached to a different creature")
        void doesNotTriggerForDifferentCreature() {
            Card equipment = createEquipment("Other Sword");
            equipment.addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new DrawCardEffect(1));
            Permanent equipPerm = new Permanent(equipment);
            equipPerm.setAttachedTo(UUID.randomUUID());
            gd.playerBattlefields.get(PLAYER1_ID).add(equipPerm);

            svc.checkEquippedCreatureDeathTriggers(gd, UUID.randomUUID(), PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Ignores non-equipment permanents attached to dying creature")
        void ignoresNonEquipment() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Some Aura");
            aura.addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEquippedCreatureDeathTriggers(gd, creaturePerm.getId(), PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card creature = createCreature("Victim", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card equipment = createEquipment("Trigger Blade");
            equipment.addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new DrawCardEffect(1));
            Permanent equipPerm = new Permanent(equipment);
            equipPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(equipPerm);

            svc.checkEquippedCreatureDeathTriggers(gd, creaturePerm.getId(), PLAYER1_ID);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Trigger Blade") && msg.contains("equipped creature died")));
        }

        @Test
        @DisplayName("Does nothing when equipment has no ON_EQUIPPED_CREATURE_DIES effects")
        void noEffectsDoesNothing() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card equipment = createEquipment("Silent Sword");
            Permanent equipPerm = new Permanent(equipment);
            equipPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(equipPerm);

            svc.checkEquippedCreatureDeathTriggers(gd, creaturePerm.getId(), PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }
    }

    // ==================== checkEnchantedPermanentDeathTriggers ====================

    @Nested
    @DisplayName("checkEnchantedPermanentDeathTriggers")
    class EnchantedPermanentDeathTriggers {

        @Test
        @DisplayName("Triggers for enchantment attached to dying permanent")
        void triggersForAttachedEnchantment() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Death Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId());

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getCard()).isEqualTo(aura);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Does not trigger for equipment (only enchantments)")
        void doesNotTriggerForEquipment() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card equipment = createEquipment("Not an Aura");
            equipment.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new DrawCardEffect(1));
            Permanent equipPerm = new Permanent(equipment);
            equipPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(equipPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId());

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not trigger for enchantment attached to different permanent")
        void doesNotTriggerForDifferentPermanent() {
            Card aura = createEnchantment("Other Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(UUID.randomUUID());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, UUID.randomUUID());

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("ReturnSourceAuraToOpponentCreatureOnDeathEffect bakes dying creature's controller ID")
        void returnAuraEffectBakesControllerId() {
            Card creature = createCreature("Plagued Creature", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Necrotic Plague");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new ReturnSourceAuraToOpponentCreatureOnDeathEffect());
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER2_ID).add(auraPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId(), PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            ReturnSourceAuraToOpponentCreatureOnDeathEffect resolved =
                    (ReturnSourceAuraToOpponentCreatureOnDeathEffect) entry.getEffectsToResolve().get(0);
            assertThat(resolved.enchantedCreatureControllerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("ReturnSourceAuraToOpponentCreatureOnDeathEffect does not bake controller when null")
        void returnAuraEffectNullControllerNotBaked() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Necrotic Plague");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new ReturnSourceAuraToOpponentCreatureOnDeathEffect());
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER2_ID).add(auraPerm);

            // Call the overload without dyingPermanentControllerId
            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId());

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            ReturnSourceAuraToOpponentCreatureOnDeathEffect resolved =
                    (ReturnSourceAuraToOpponentCreatureOnDeathEffect) entry.getEffectsToResolve().get(0);
            assertThat(resolved.enchantedCreatureControllerId()).isNull();
        }

        @Test
        @DisplayName("Triggers across players — enchantment on player2 attached to player1's permanent")
        void triggersAcrossPlayers() {
            Card creature = createCreature("Target", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Cross Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER2_ID).add(auraPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getControllerId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Log Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId());

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    argThat(msg -> msg.contains("Log Aura") && msg.contains("enchanted permanent put into graveyard")));
        }

        @Test
        @DisplayName("Does nothing when enchantment has no ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD effects")
        void noEffectsDoesNothing() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Silent Aura");
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId());

            assertThat(gd.stack).isEmpty();
        }
    }

    // ==================== checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers ====================

    @Nested
    @DisplayName("checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers")
    class AnyArtifactGraveyardTriggers {

        @Test
        @DisplayName("Triggers for permanent with ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD effect")
        void triggersForAnyArtifactEffect() {
            Card watcher = createArtifact("Watcher Artifact");
            watcher.addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER1_ID, PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("DealDamageToTriggeringPermanentControllerEffect gets target set to artifact controller")
        void damageControllerEffectSetsTarget() {
            Card watcher = createArtifact("Damage Watcher");
            watcher.addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new DealDamageToTriggeringPermanentControllerEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER2_ID, PLAYER2_ID);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getTargetId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("MayEffect queues may ability on stack")
        void mayEffectQueuesMayAbility() {
            Card watcher = createArtifact("Optional Watcher");
            watcher.addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new MayEffect(new DrawCardEffect(1), "Draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER1_ID, PLAYER1_ID);

            // CR 603.5 — "you may" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("Opponent artifact trigger only fires for opponent's artifacts")
        void opponentTriggerOnlyFiresForOpponent() {
            Card watcher = createArtifact("Opponent Watcher");
            watcher.addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            // Artifact goes into opponent's (player2) graveyard — should trigger
            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER2_ID, PLAYER2_ID);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Opponent artifact trigger does NOT fire for own artifacts")
        void opponentTriggerDoesNotFireForOwn() {
            Card watcher = createArtifact("Opponent Watcher");
            watcher.addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            // Artifact goes into own (player1) graveyard — should NOT trigger
            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER1_ID, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card watcher = createArtifact("Log Watcher");
            watcher.addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER1_ID, PLAYER1_ID);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Log Watcher") && msg.contains("triggers")));
        }

        @Test
        @DisplayName("Non-DealDamage effect has null targetId on stack entry")
        void nonDealDamageEffectHasNullTargetId() {
            Card watcher = createArtifact("Plain Watcher");
            watcher.addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER1_ID, PLAYER2_ID);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTargetId()).isNull();
        }

        @Test
        @DisplayName("sourcePermanentId is set on stack entry")
        void setsSourcePermanentId() {
            Card watcher = createArtifact("Tracked Watcher");
            watcher.addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            Permanent watcherPerm = addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER1_ID, PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(watcherPerm.getId());
        }

        @Test
        @DisplayName("Opponent-specific MayEffect queues may ability")
        void opponentMayEffectQueuesMayAbility() {
            Card watcher = createArtifact("Opponent May Watcher");
            watcher.addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                    new MayEffect(new DrawCardEffect(1), "Draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER2_ID, PLAYER2_ID);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("Opponent-specific non-MayEffect sets sourcePermanentId on stack entry")
        void opponentNonMayEffectSetsSourcePermanentId() {
            Card watcher = createArtifact("Opponent Tracker");
            watcher.addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            Permanent watcherPerm = addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER2_ID, PLAYER2_ID);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(watcherPerm.getId());
        }

        @Test
        @DisplayName("Both ANY and OPPONENT triggers fire independently from same permanent")
        void bothAnyAndOpponentTriggersFireIndependently() {
            Card watcher = createArtifact("Dual Watcher");
            watcher.addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            watcher.addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, new GainLifeEffect(2));
            addToBattlefield(PLAYER1_ID, watcher);

            // Opponent's artifact goes to graveyard — both triggers should fire
            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER2_ID, PLAYER2_ID);

            assertThat(gd.stack).hasSize(2);
        }
    }

    // ==================== checkAnyCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkAnyCreatureDeathTriggers")
    class AnyCreatureDeathTriggers {

        @Test
        @DisplayName("Does nothing when no permanents have ON_ANY_CREATURE_DIES effects")
        void noEffectsDoesNothing() {
            addToBattlefield(PLAYER1_ID, createCreature("Vanilla", 2, 2));

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, createCreature("Dying Creature", 1, 1));

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).isEmpty();
        }

        @Test
        @DisplayName("Non-targeting effect adds triggered ability to stack without sourcePermanentId")
        void nonTargetingEffectAddsToStack() {
            Card watcher = createCreature("Death Counter", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, createCreature("Dying Creature", 1, 1));

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(watcher);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
            assertThat(entry.getSourcePermanentId()).isNull();
        }

        @Test
        @DisplayName("Targeting effect queues DeathTriggerTarget")
        void targetingEffectQueuesDeathTriggerTarget() {
            Card watcher = createCreature("Target Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutChargeCounterOnTargetPermanentEffect());
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, createCreature("Dying Creature", 1, 1));

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
            PermanentChoiceContext.DeathTriggerTarget target = gd.pendingDeathTriggerTargets.peek();
            assertThat(target.dyingCard()).isEqualTo(watcher);
            assertThat(target.controllerId()).isEqualTo(PLAYER1_ID);
            assertThat(target.effects().get(0)).isInstanceOf(PutChargeCounterOnTargetPermanentEffect.class);
        }

        @Test
        @DisplayName("MayEffect queues may ability on stack")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("Optional Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MayEffect(new DrawCardEffect(1), "Draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, createCreature("Dying Creature", 1, 1));

            // CR 603.5 — "you may" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("PutCountersOnSourceEffect adds to stack with sourcePermanentId")
        void putCountersOnSourceSetsSourcePermanentId() {
            Card watcher = createCreature("Growing Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
            Permanent watcherPerm = addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, createCreature("Dying Creature", 1, 1));

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getSourcePermanentId()).isEqualTo(watcherPerm.getId());
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(PutCountersOnSourceEffect.class);
        }

        @Test
        @DisplayName("Triggers across all players' battlefields")
        void triggersAcrossPlayers() {
            Card watcher1 = createCreature("P1 Watcher", 1, 1);
            watcher1.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher1);

            Card watcher2 = createCreature("P2 Watcher", 1, 1);
            watcher2.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new DrawCardEffect(2));
            addToBattlefield(PLAYER2_ID, watcher2);

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, createCreature("Dying Creature", 1, 1));

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.get(0).getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(gd.stack.get(1).getControllerId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card watcher = createCreature("Morbid Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, createCreature("Dying Creature", 1, 1));

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Morbid Watcher") && msg.contains("triggers")));
        }
    }

    // ==================== checkAllyNontokenCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkAllyNontokenCreatureDeathTriggers")
    class AllyNontokenCreatureDeathTriggers {

        @Test
        @DisplayName("Does nothing when dying card is a token")
        void tokenCardDoesNothing() {
            Card token = createCreature("Token", 1, 1);
            token.setToken(true);

            Card watcher = createCreature("Ally Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, token);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when battlefield is null")
        void nullBattlefieldDoesNothing() {
            Card dying = createCreature("Dying Guy", 2, 2);
            gd.playerBattlefields.remove(PLAYER1_ID);

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, dying);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when no permanents have ON_ALLY_NONTOKEN_CREATURE_DIES effects")
        void noEffectsDoesNothing() {
            Card dying = createCreature("Dying Guy", 2, 2);
            addToBattlefield(PLAYER1_ID, createCreature("Vanilla", 2, 2));

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, dying);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Non-may effect adds to stack with sourcePermanentId")
        void nonMayEffectAddsToStackWithSourcePermanentId() {
            Card dying = createCreature("Dying Guy", 2, 2);

            Card watcher = createCreature("Ally Tracker", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new DrawCardEffect(1));
            Permanent watcherPerm = addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, dying);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(watcher);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getSourcePermanentId()).isEqualTo(watcherPerm.getId());
        }

        @Test
        @DisplayName("MayEffect queues may ability on stack")
        void mayEffectQueuesMayAbility() {
            Card dying = createCreature("Dying Guy", 2, 2);

            Card watcher = createCreature("May Ally Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new MayEffect(new DrawCardEffect(1), "Draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, dying);

            // CR 603.5 — "you may" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("Only triggers for permanents controlled by dying creature's controller")
        void onlyOwnBattlefield() {
            Card dying = createCreature("Dying Guy", 2, 2);

            Card watcher = createCreature("Enemy Ally Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER2_ID, watcher);

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, dying);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card dying = createCreature("Dying Guy", 2, 2);

            Card watcher = createCreature("Morbid Ally", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, dying);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Morbid Ally") && msg.contains("triggers")));
        }
    }

    // ==================== checkAnyNontokenCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkAnyNontokenCreatureDeathTriggers")
    class AnyNontokenCreatureDeathTriggers {

        @Test
        @DisplayName("Does nothing when dying card is a token")
        void tokenCardDoesNothing() {
            Card token = createCreature("Token", 1, 1);
            token.setToken(true);

            Card watcher = createCreature("Imprint Watcher", 0, 1);
            watcher.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayEffect(new ImprintDyingCreatureEffect(), "Imprint?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyNontokenCreatureDeathTriggers(gd, token);

            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Imprint trigger queues may ability with dying card ID baked in")
        void imprintTriggerQueuesMayAbilityWithDyingCardId() {
            Card dying = createCreature("Dying Nontoken", 3, 3);

            Card watcher = createCreature("Mimic Vat", 0, 0);
            watcher.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayEffect(new ImprintDyingCreatureEffect(), "Exile and imprint?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyNontokenCreatureDeathTriggers(gd, dying);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            PendingMayAbility pending = gd.pendingMayAbilities.get(0);
            assertThat(pending.effects().get(0)).isInstanceOf(ImprintDyingCreatureEffect.class);
            ImprintDyingCreatureEffect imprint = (ImprintDyingCreatureEffect) pending.effects().get(0);
            assertThat(imprint.dyingCardId()).isEqualTo(dying.getId());
        }

        @Test
        @DisplayName("ReturnDyingCreature trigger only fires when dying card is in controller's graveyard")
        void returnTriggerRequiresCardInGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);

            Card deathmantle = createEquipment("Nim Deathmantle");
            deathmantle.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayPayManaEffect("{4}", new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(), "Pay 4 to return?"));
            addToBattlefield(PLAYER1_ID, deathmantle);

            // Card is NOT in player1's graveyard
            svc.checkAnyNontokenCreatureDeathTriggers(gd, dying);

            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("ReturnDyingCreature trigger fires when dying card IS in controller's graveyard")
        void returnTriggerFiresWhenInGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);

            Card deathmantle = createEquipment("Nim Deathmantle");
            deathmantle.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayPayManaEffect("{4}", new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(), "Pay 4 to return?"));
            addToBattlefield(PLAYER1_ID, deathmantle);

            // Put dying card in player1's graveyard
            gd.playerGraveyards.get(PLAYER1_ID).add(dying);

            svc.checkAnyNontokenCreatureDeathTriggers(gd, dying);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            PendingMayAbility pending = gd.pendingMayAbilities.get(0);
            assertThat(pending.manaCost()).isEqualTo("{4}");
            assertThat(pending.targetCardId()).isEqualTo(dying.getId());
            assertThat(pending.effects().get(0)).isInstanceOf(ReturnDyingCreatureToBattlefieldAndAttachSourceEffect.class);
        }

        @Test
        @DisplayName("Logs imprint trigger message")
        void logsImprintMessage() {
            Card dying = createCreature("Dead Guy", 1, 1);

            Card watcher = createCreature("Vat", 0, 0);
            watcher.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayEffect(new ImprintDyingCreatureEffect(), "Imprint?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyNontokenCreatureDeathTriggers(gd, dying);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Vat") && msg.contains("imprint")));
        }

        @Test
        @DisplayName("ReturnDyingCreature trigger logs message when fired")
        void returnTriggerLogsMessage() {
            Card dying = createCreature("Dead Creature", 2, 2);

            Card deathmantle = createEquipment("Nim Deathmantle");
            deathmantle.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayPayManaEffect("{4}", new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(), "Pay 4 to return?"));
            addToBattlefield(PLAYER1_ID, deathmantle);

            gd.playerGraveyards.get(PLAYER1_ID).add(dying);

            svc.checkAnyNontokenCreatureDeathTriggers(gd, dying);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    argThat(msg -> msg.contains("Nim Deathmantle") && msg.contains("Dead Creature") && msg.contains("died")));
        }

        @Test
        @DisplayName("ReturnDyingCreature trigger does NOT fire when graveyard is null")
        void returnTriggerNullGraveyardDoesNotFire() {
            Card dying = createCreature("Dead Creature", 2, 2);

            Card deathmantle = createEquipment("Nim Deathmantle");
            deathmantle.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayPayManaEffect("{4}", new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(), "Pay 4 to return?"));
            addToBattlefield(PLAYER1_ID, deathmantle);

            gd.playerGraveyards.remove(PLAYER1_ID);

            svc.checkAnyNontokenCreatureDeathTriggers(gd, dying);

            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("MayEffect wrapping non-Imprint effect is silently skipped")
        void mayEffectWrappingNonImprintDoesNothing() {
            Card dying = createCreature("Dead Creature", 2, 2);

            Card watcher = createCreature("Unrecognized Watcher", 0, 0);
            watcher.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayEffect(new DrawCardEffect(1), "Draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyNontokenCreatureDeathTriggers(gd, dying);

            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("MayPayManaEffect wrapping non-ReturnDyingCreature effect is silently skipped")
        void mayPayManaEffectWrappingNonReturnDyingDoesNothing() {
            Card dying = createCreature("Dead Creature", 2, 2);

            Card watcher = createCreature("Unrecognized Pay Watcher", 0, 0);
            watcher.addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                    new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay to draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyNontokenCreatureDeathTriggers(gd, dying);

            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ==================== checkOpponentCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkOpponentCreatureDeathTriggers")
    class OpponentCreatureDeathTriggers {

        @Test
        @DisplayName("Triggers for permanent controlled by opponent of dying creature's controller")
        void triggersForOpponentPermanent() {
            Card watcher = createCreature("Vulture", 1, 1);
            watcher.addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER2_ID, watcher);

            svc.checkOpponentCreatureDeathTriggers(gd, PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER2_ID);
            assertThat(entry.getTargetId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Does NOT trigger for permanent controlled by same player as dying creature")
        void doesNotTriggerForSamePlayer() {
            Card watcher = createCreature("Vulture", 1, 1);
            watcher.addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkOpponentCreatureDeathTriggers(gd, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("Optional Vulture", 1, 1);
            watcher.addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new MayEffect(new DrawCardEffect(1), "Draw?"));
            addToBattlefield(PLAYER2_ID, watcher);

            svc.checkOpponentCreatureDeathTriggers(gd, PLAYER1_ID);

            // CR 603.5 — "you may" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card watcher = createCreature("Death Profiteer", 1, 1);
            watcher.addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER2_ID, watcher);

            svc.checkOpponentCreatureDeathTriggers(gd, PLAYER1_ID);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Death Profiteer") && msg.contains("triggers")));
        }

        @Test
        @DisplayName("Sets sourcePermanentId on stack entry")
        void setsSourcePermanentId() {
            Card watcher = createCreature("Tracker", 1, 1);
            watcher.addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new DrawCardEffect(1));
            Permanent watcherPerm = addToBattlefield(PLAYER2_ID, watcher);

            svc.checkOpponentCreatureDeathTriggers(gd, PLAYER1_ID);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(watcherPerm.getId());
        }
    }

    // ==================== checkEnchantedPermanentLTBTriggers ====================

    @Nested
    @DisplayName("checkEnchantedPermanentLTBTriggers")
    class EnchantedPermanentLTBTriggers {

        @Test
        @DisplayName("Non-conditional effect fires unconditionally for attached enchantment")
        void nonConditionalEffectFiresUnconditionally() {
            Card creature = createCreature("Leaving Guy", 3, 3);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("LTB Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(aura);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getEffectsToResolve()).hasSize(1);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("Conditional effect fires when leaving permanent matches filter")
        void conditionalEffectMatchingFilterFires() {
            Card creature = createCreature("Big Creature", 4, 4);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            CardTypePredicate creatureFilter = new CardTypePredicate(CardType.CREATURE);
            List<CardEffect> resolvedEffects = List.of(new DrawCardEffect(2));
            EnchantedPermanentLeavesConditionalEffect conditional =
                    new EnchantedPermanentLeavesConditionalEffect(creatureFilter, resolvedEffects);

            Card aura = createEnchantment("Conditional Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, conditional);
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            when(gameQueryService.matchesCardPredicate(creature, creatureFilter, null)).thenReturn(true);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEffectsToResolve()).hasSize(1);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("Conditional effect does NOT fire when leaving permanent does not match filter")
        void conditionalEffectNonMatchingFilterDoesNotFire() {
            Card artifact = createArtifact("Leaving Artifact");
            Permanent artifactPerm = new Permanent(artifact);
            gd.playerBattlefields.get(PLAYER1_ID).add(artifactPerm);

            CardTypePredicate creatureFilter = new CardTypePredicate(CardType.CREATURE);
            List<CardEffect> resolvedEffects = List.of(new DrawCardEffect(2));
            EnchantedPermanentLeavesConditionalEffect conditional =
                    new EnchantedPermanentLeavesConditionalEffect(creatureFilter, resolvedEffects);

            Card aura = createEnchantment("Conditional Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, conditional);
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(artifactPerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            when(gameQueryService.matchesCardPredicate(artifact, creatureFilter, null)).thenReturn(false);

            svc.checkEnchantedPermanentLTBTriggers(gd, artifactPerm);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Conditional effect with null filter fires unconditionally")
        void conditionalEffectNullFilterFires() {
            Card creature = createCreature("Any Permanent", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            List<CardEffect> resolvedEffects = List.of(new DrawCardEffect(1));
            EnchantedPermanentLeavesConditionalEffect conditional =
                    new EnchantedPermanentLeavesConditionalEffect(null, resolvedEffects);

            Card aura = createEnchantment("Open Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, conditional);
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Does not trigger for equipment attached to leaving permanent")
        void ignoresEquipment() {
            Card creature = createCreature("Leaving Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card equipment = createEquipment("Attached Sword");
            equipment.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
            Permanent equipPerm = new Permanent(equipment);
            equipPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(equipPerm);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not trigger for enchantment attached to a different permanent")
        void doesNotTriggerForDifferentPermanent() {
            Card creature = createCreature("Leaving Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Other Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(UUID.randomUUID()); // attached to something else
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Triggers across players — enchantment on player2 attached to player1's permanent")
        void triggersAcrossPlayers() {
            Card creature = createCreature("Target", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Cross Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER2_ID).add(auraPerm);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getControllerId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card creature = createCreature("Leaving Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Tracker Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    argThat(msg -> msg.contains("Tracker Aura") && msg.contains("enchanted permanent left the battlefield")));
        }

        @Test
        @DisplayName("Does nothing when enchantment has no LTB effects")
        void noEffectsDoesNothing() {
            Card creature = createCreature("Leaving Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Silent Aura");
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Conditional effect with multiple resolvedEffects puts all as single stack entry")
        void conditionalEffectMultipleResolvedEffectsSingleStackEntry() {
            Card creature = createCreature("Big Creature", 4, 4);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            List<CardEffect> resolvedEffects = List.of(new DrawCardEffect(2), new GainLifeEffect(3));
            EnchantedPermanentLeavesConditionalEffect conditional =
                    new EnchantedPermanentLeavesConditionalEffect(null, resolvedEffects);

            Card aura = createEnchantment("Multi Aura");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, conditional);
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentLTBTriggers(gd, creaturePerm);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEffectsToResolve()).hasSize(2);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
            assertThat(entry.getEffectsToResolve().get(1)).isInstanceOf(GainLifeEffect.class);
        }
    }

    // ==================== checkSelfLeavesTriggered ====================

    @Nested
    @DisplayName("checkSelfLeavesTriggered")
    class SelfLeavesTriggered {

        @Test
        @DisplayName("Does nothing when card has no ON_SELF_LEAVES_BATTLEFIELD effects")
        void noEffectsDoesNothing() {
            Card card = createCreature("Vanilla", 2, 2);
            Permanent perm = new Permanent(card);

            svc.checkSelfLeavesTriggered(gd, perm, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Generic effect adds triggered ability to stack")
        void genericEffectAddsToStack() {
            Card card = createCreature("Leaving Dude", 2, 2);
            card.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
            Permanent perm = new Permanent(card);

            svc.checkSelfLeavesTriggered(gd, perm, PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(card);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getDescription()).isEqualTo("Leaving Dude's ability");
            assertThat(entry.getEffectsToResolve()).hasSize(1);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("ControllerLosesGameOnLeavesEffect is converted to TargetPlayerLosesGameEffect")
        void controllerLosesGameConvertsToTargetPlayerLosesGame() {
            Card card = createCreature("Pact Creature", 5, 5);
            card.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ControllerLosesGameOnLeavesEffect());
            Permanent perm = new Permanent(card);

            svc.checkSelfLeavesTriggered(gd, perm, PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEffectsToResolve()).hasSize(1);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(TargetPlayerLosesGameEffect.class);
            TargetPlayerLosesGameEffect resolved = (TargetPlayerLosesGameEffect) entry.getEffectsToResolve().get(0);
            assertThat(resolved.playerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("ControllerLosesGameOnLeavesEffect targets the correct controller")
        void controllerLosesGameTargetsCorrectPlayer() {
            Card card = createCreature("Pact Creature", 5, 5);
            card.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ControllerLosesGameOnLeavesEffect());
            Permanent perm = new Permanent(card);

            svc.checkSelfLeavesTriggered(gd, perm, PLAYER2_ID);

            TargetPlayerLosesGameEffect resolved =
                    (TargetPlayerLosesGameEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.playerId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Multiple effects all get processed")
        void multipleEffectsAllProcessed() {
            Card card = createCreature("Multi-Leave", 2, 2);
            card.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
            card.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new GainLifeEffect(3));
            Permanent perm = new Permanent(card);

            svc.checkSelfLeavesTriggered(gd, perm, PLAYER1_ID);

            assertThat(gd.stack).hasSize(2);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card card = createCreature("Logged Leaver", 2, 2);
            card.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
            Permanent perm = new Permanent(card);

            svc.checkSelfLeavesTriggered(gd, perm, PLAYER1_ID);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    argThat(msg -> msg.contains("Logged Leaver") && msg.contains("left the battlefield")));
        }
    }

    // ==================== checkAllyAuraOrEquipmentPutIntoGraveyardTriggers ====================

    @Nested
    @DisplayName("checkAllyAuraOrEquipmentPutIntoGraveyardTriggers")
    class AllyAuraOrEquipmentGraveyardTriggers {

        @Test
        @DisplayName("Does nothing when battlefield is null")
        void nullBattlefieldDoesNothing() {
            gd.playerBattlefields.remove(PLAYER1_ID);
            Card dyingAura = createEnchantment("Dying Aura");

            svc.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gd, dyingAura, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when no permanents have the trigger effect")
        void noEffectsDoesNothing() {
            addToBattlefield(PLAYER1_ID, createCreature("Vanilla", 2, 2));
            Card dyingAura = createEnchantment("Dying Aura");

            svc.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gd, dyingAura, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Queues MayEffect with delayed return for RegisterDelayedReturnCardFromGraveyardToHandEffect")
        void registerDelayedReturnQueuesMayEffect() {
            Card dyingAura = createEnchantment("Dying Aura");

            Card tiana = createCreature("Tiana", 3, 3);
            tiana.addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
            addToBattlefield(PLAYER1_ID, tiana);

            svc.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gd, dyingAura, PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(tiana);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getEffectsToResolve()).hasSize(1);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
            MayEffect may = (MayEffect) entry.getEffectsToResolve().get(0);
            assertThat(may.wrapped()).isInstanceOf(RegisterDelayedReturnCardFromGraveyardToHandEffect.class);
            RegisterDelayedReturnCardFromGraveyardToHandEffect delayedEffect =
                    (RegisterDelayedReturnCardFromGraveyardToHandEffect) may.wrapped();
            assertThat(delayedEffect.cardId()).isEqualTo(dyingAura.getId());
        }

        @Test
        @DisplayName("May prompt includes dying card name")
        void mayPromptIncludesDyingCardName() {
            Card dyingEquipment = createEquipment("Lost Sword");

            Card tiana = createCreature("Tiana", 3, 3);
            tiana.addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
            addToBattlefield(PLAYER1_ID, tiana);

            svc.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gd, dyingEquipment, PLAYER1_ID);

            MayEffect may = (MayEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(may.prompt()).contains("Lost Sword");
        }

        @Test
        @DisplayName("Only triggers for permanents controlled by the same player")
        void onlyOwnBattlefield() {
            Card dyingAura = createEnchantment("Dying Aura");

            Card tiana = createCreature("Enemy Tiana", 3, 3);
            tiana.addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
            addToBattlefield(PLAYER2_ID, tiana);

            svc.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gd, dyingAura, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Ignores non-RegisterDelayedReturn effects")
        void ignoresNonDelayedReturnEffects() {
            Card dyingAura = createEnchantment("Dying Aura");

            Card watcher = createCreature("Other Watcher", 2, 2);
            watcher.addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gd, dyingAura, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card dyingAura = createEnchantment("Fallen Aura");

            Card tiana = createCreature("Tiana Ship's Caretaker", 3, 3);
            tiana.addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
            addToBattlefield(PLAYER1_ID, tiana);

            svc.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gd, dyingAura, PLAYER1_ID);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    argThat(msg -> msg.contains("Tiana Ship's Caretaker") && msg.contains("Fallen Aura")));
        }

        @Test
        @DisplayName("Multiple permanents with the trigger all fire")
        void multiplePermanentsWithTriggerAllFire() {
            Card dyingAura = createEnchantment("Dying Aura");

            Card tiana1 = createCreature("Tiana A", 3, 3);
            tiana1.addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
            addToBattlefield(PLAYER1_ID, tiana1);

            Card tiana2 = createCreature("Tiana B", 3, 3);
            tiana2.addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                    new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
            addToBattlefield(PLAYER1_ID, tiana2);

            svc.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gd, dyingAura, PLAYER1_ID);

            assertThat(gd.stack).hasSize(2);
        }
    }

    // ==================== checkEnchantedPermanentDeathTriggers (ReturnEnchantedCreatureToOwnerHandOnDeathEffect) ====================

    @Nested
    @DisplayName("checkEnchantedPermanentDeathTriggers — ReturnEnchantedCreatureToOwnerHandOnDeathEffect")
    class EnchantedPermanentDeathTriggersReturnToHand {

        @Test
        @DisplayName("ReturnEnchantedCreatureToOwnerHandOnDeathEffect bakes dying creature's card ID")
        void returnToHandBakesDyingCreatureCardId() {
            Card creature = createCreature("Enchanted Creature", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Demonic Vigor");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new ReturnEnchantedCreatureToOwnerHandOnDeathEffect());
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId(), PLAYER1_ID, creature.getId());

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            ReturnEnchantedCreatureToOwnerHandOnDeathEffect resolved =
                    (ReturnEnchantedCreatureToOwnerHandOnDeathEffect) entry.getEffectsToResolve().get(0);
            assertThat(resolved.dyingCreatureCardId()).isEqualTo(creature.getId());
        }

        @Test
        @DisplayName("ReturnEnchantedCreatureToOwnerHandOnDeathEffect does not bake card ID when null")
        void returnToHandNullCardIdNotBaked() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card aura = createEnchantment("Demonic Vigor");
            aura.addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new ReturnEnchantedCreatureToOwnerHandOnDeathEffect());
            Permanent auraPerm = new Permanent(aura);
            auraPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(auraPerm);

            svc.checkEnchantedPermanentDeathTriggers(gd, creaturePerm.getId(), PLAYER1_ID, null);

            assertThat(gd.stack).hasSize(1);
            ReturnEnchantedCreatureToOwnerHandOnDeathEffect resolved =
                    (ReturnEnchantedCreatureToOwnerHandOnDeathEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.dyingCreatureCardId()).isNull();
        }
    }

    // ==================== checkEquippedCreatureDeathTriggers — targeting effects ====================

    @Nested
    @DisplayName("checkEquippedCreatureDeathTriggers — targeting effects")
    class EquippedCreatureDeathTriggersTargeting {

        @Test
        @DisplayName("Targeting effect queues DeathTriggerTarget instead of stack")
        void targetingEffectQueuesDeathTriggerTarget() {
            Card creature = createCreature("Dying Guy", 2, 2);
            Permanent creaturePerm = new Permanent(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(creaturePerm);

            Card equipment = createEquipment("Target Sword");
            equipment.addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new PutChargeCounterOnTargetPermanentEffect());
            Permanent equipPerm = new Permanent(equipment);
            equipPerm.setAttachedTo(creaturePerm.getId());
            gd.playerBattlefields.get(PLAYER1_ID).add(equipPerm);

            svc.checkEquippedCreatureDeathTriggers(gd, creaturePerm.getId(), PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
            PermanentChoiceContext.DeathTriggerTarget target = gd.pendingDeathTriggerTargets.peek();
            assertThat(target.dyingCard()).isEqualTo(equipment);
            assertThat(target.controllerId()).isEqualTo(PLAYER1_ID);
            assertThat(target.effects().get(0)).isInstanceOf(PutChargeCounterOnTargetPermanentEffect.class);
        }
    }

    // ==================== checkAnyCreatureDeathTriggers — SubtypeConditionalEffect ====================

    @Nested
    @DisplayName("checkAnyCreatureDeathTriggers — SubtypeConditionalEffect")
    class AnyCreatureDeathTriggersSubtype {

        @Test
        @DisplayName("SubtypeConditionalEffect fires when dying creature has matching subtype")
        void subtypeConditionalMatchingSubtypeFires() {
            stubSubtypeChecks();
            Card watcher = createCreature("Zombie Lord", 2, 2);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                    new SubtypeConditionalEffect(CardSubtype.ZOMBIE, new DrawCardEffect(1)));
            addToBattlefield(PLAYER1_ID, watcher);

            Card dyingZombie = createCreature("Zombie", 1, 1);
            dyingZombie.setSubtypes(List.of(CardSubtype.ZOMBIE));

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, dyingZombie);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("SubtypeConditionalEffect does NOT fire when dying creature lacks subtype")
        void subtypeConditionalNonMatchingSubtypeDoesNotFire() {
            stubSubtypeChecksWithGrantedLookup();
            Card watcher = createCreature("Zombie Lord", 2, 2);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                    new SubtypeConditionalEffect(CardSubtype.ZOMBIE, new DrawCardEffect(1)));
            addToBattlefield(PLAYER1_ID, watcher);

            Card dyingBear = createCreature("Grizzly Bears", 2, 2);
            dyingBear.setSubtypes(List.of(CardSubtype.BEAR));

            svc.checkAnyCreatureDeathTriggers(gd, PLAYER1_ID, dyingBear);

            assertThat(gd.stack).isEmpty();
        }
    }
}
