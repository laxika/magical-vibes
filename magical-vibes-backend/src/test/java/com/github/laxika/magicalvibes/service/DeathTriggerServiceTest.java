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
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
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
        void noEffects_doesNothing() {
            Card card = createCreature("Vanilla", 2, 2);

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).isEmpty();
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Non-targeting effect adds triggered ability to stack")
        void nonTargetingEffect_addsToStack() {
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
        void permanentTargetingEffect_queuesDeathTriggerTarget() {
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
        void mayEffect_queuesMayAbility() {
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
        void mayPayManaEffect_queuesMayAbility() {
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
        void losesLifeEqualToPower_bakesPower() {
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
        void losesLifeEqualToPower_fallsBackToCardPower() {
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
        void losesLifeEqualToPower_clampsNegativePowerToZero() {
            Card card = createCreature("Weakened", 2, 2);
            card.addEffect(EffectSlot.ON_DEATH, new TargetPlayerLosesLifeEqualToPowerEffect());
            Permanent perm = new Permanent(card);
            perm.setPowerModifier(-5);

            svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

            TargetPlayerLosesLifeEffect resolved =
                    (TargetPlayerLosesLifeEffect) gd.pendingDeathTriggerTargets.peek().effects().get(0);
            assertThat(resolved.amount()).isEqualTo(0);
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
                perm.getBlockingTargetPermanentIds().add(attackerId);
                gd.currentStep = TurnStep.COMBAT_DAMAGE;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

                assertThat(gd.stack).hasSize(1);
                StackEntry entry = gd.stack.get(0);
                assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DealDamageToBlockedAttackersOnDeathEffect.class);
                assertThat(entry.getTargetPermanentIds()).containsExactly(attackerId);
            }

            @Test
            @DisplayName("Does not trigger outside combat")
            void doesNotTriggerOutsideCombat() {
                Card card = createCreature("Thorny Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                Permanent perm = new Permanent(card);
                perm.getBlockingTargetPermanentIds().add(UUID.randomUUID());
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
            @DisplayName("Triggers at BEGINNING_OF_COMBAT step")
            void triggersAtBeginningOfCombat() {
                Card card = createCreature("Thorny Blocker", 1, 1);
                card.addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(2));
                Permanent perm = new Permanent(card);
                perm.getBlockingTargetPermanentIds().add(UUID.randomUUID());
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
                perm.getBlockingTargetPermanentIds().add(UUID.randomUUID());
                gd.currentStep = TurnStep.END_OF_COMBAT;

                svc.collectDeathTrigger(gd, card, PLAYER1_ID, true, perm);

                assertThat(gd.stack).hasSize(1);
            }
        }

        @Test
        @DisplayName("Multiple effects on one card all get processed")
        void multipleEffects_allProcessed() {
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
        void noDelayedTrigger_doesNothing() {
            svc.triggerDelayedPoisonOnDeath(gd, UUID.randomUUID(), PLAYER1_ID);

            assertThat(gd.playerPoisonCounters.get(PLAYER1_ID)).isNull();
        }

        @Test
        @DisplayName("Does nothing when poison amount is zero")
        void zeroPoisonAmount_doesNothing() {
            UUID cardId = UUID.randomUUID();
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(cardId, 0);

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
        void cannotGetPoisonCounters_doesNothing() {
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

        @Test
        @DisplayName("Does nothing when battlefield is null")
        void nullBattlefield_doesNothing() {
            gd.playerBattlefields.remove(PLAYER1_ID);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when no permanents have ON_ALLY_CREATURE_DIES effects")
        void noEffects_doesNothing() {
            addToBattlefield(PLAYER1_ID, createCreature("Vanilla", 2, 2));

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Adds triggered ability to stack for non-may effect")
        void nonMayEffect_addsToStack() {
            Card watcher = createCreature("Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(watcher);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Queues may ability for MayEffect")
        void mayEffect_queuesMayAbility() {
            Card watcher = createCreature("May Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new MayEffect(new DrawCardEffect(1), "Draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID);

            // CR 603.5 — "you may" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("Queues may ability for MayPayManaEffect")
        void mayPayManaEffect_queuesMayAbility() {
            Card watcher = createCreature("Pay Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new MayPayManaEffect("{1}", new DrawCardEffect(1), "Pay to draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID);

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

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Blood Artist") && msg.contains("triggers")));
        }

        @Test
        @DisplayName("Only triggers for permanents controlled by dying creature's controller")
        void onlyOwnBattlefield() {
            Card watcher = createCreature("Enemy Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER2_ID, watcher);

            svc.checkAllyCreatureDeathTriggers(gd, PLAYER1_ID);

            assertThat(gd.stack).isEmpty();
        }
    }

    // ==================== checkEquippedCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkEquippedCreatureDeathTriggers")
    class EquippedCreatureDeathTriggers {

        @Test
        @DisplayName("Does nothing when battlefield is null")
        void nullBattlefield_doesNothing() {
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
        void returnAuraEffect_bakesControllerId() {
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
        void returnAuraEffect_nullControllerNotBaked() {
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
        void damageControllerEffect_setsTarget() {
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
        void mayEffect_queuesMayAbility() {
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
        void opponentTrigger_onlyFiresForOpponent() {
            Card watcher = createArtifact("Opponent Watcher");
            watcher.addEffect(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            // Artifact goes into opponent's (player2) graveyard — should trigger
            svc.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gd, PLAYER2_ID, PLAYER2_ID);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Opponent artifact trigger does NOT fire for own artifacts")
        void opponentTrigger_doesNotFireForOwn() {
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
    }

    // ==================== checkAnyCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkAnyCreatureDeathTriggers")
    class AnyCreatureDeathTriggers {

        @Test
        @DisplayName("Does nothing when no permanents have ON_ANY_CREATURE_DIES effects")
        void noEffects_doesNothing() {
            addToBattlefield(PLAYER1_ID, createCreature("Vanilla", 2, 2));

            svc.checkAnyCreatureDeathTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).isEmpty();
        }

        @Test
        @DisplayName("Non-targeting effect adds triggered ability to stack")
        void nonTargetingEffect_addsToStack() {
            Card watcher = createCreature("Death Counter", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new DrawCardEffect(1));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isEqualTo(watcher);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("Targeting effect queues DeathTriggerTarget")
        void targetingEffect_queuesDeathTriggerTarget() {
            Card watcher = createCreature("Target Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutChargeCounterOnTargetPermanentEffect());
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
            PermanentChoiceContext.DeathTriggerTarget target = gd.pendingDeathTriggerTargets.peek();
            assertThat(target.dyingCard()).isEqualTo(watcher);
            assertThat(target.controllerId()).isEqualTo(PLAYER1_ID);
            assertThat(target.effects().get(0)).isInstanceOf(PutChargeCounterOnTargetPermanentEffect.class);
        }

        @Test
        @DisplayName("MayEffect queues may ability on stack")
        void mayEffect_queuesMayAbility() {
            Card watcher = createCreature("Optional Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MayEffect(new DrawCardEffect(1), "Draw?"));
            addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd);

            // CR 603.5 — "you may" triggered abilities go on the stack immediately
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("PutCountersOnSourceEffect adds to stack with sourcePermanentId")
        void putCountersOnSource_setsSourcePermanentId() {
            Card watcher = createCreature("Growing Watcher", 1, 1);
            watcher.addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
            Permanent watcherPerm = addToBattlefield(PLAYER1_ID, watcher);

            svc.checkAnyCreatureDeathTriggers(gd);

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

            svc.checkAnyCreatureDeathTriggers(gd);

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

            svc.checkAnyCreatureDeathTriggers(gd);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Morbid Watcher") && msg.contains("triggers")));
        }
    }

    // ==================== checkAllyNontokenCreatureDeathTriggers ====================

    @Nested
    @DisplayName("checkAllyNontokenCreatureDeathTriggers")
    class AllyNontokenCreatureDeathTriggers {

        @Test
        @DisplayName("Does nothing when dying card is a token")
        void tokenCard_doesNothing() {
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
        void nullBattlefield_doesNothing() {
            Card dying = createCreature("Dying Guy", 2, 2);
            gd.playerBattlefields.remove(PLAYER1_ID);

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, dying);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when no permanents have ON_ALLY_NONTOKEN_CREATURE_DIES effects")
        void noEffects_doesNothing() {
            Card dying = createCreature("Dying Guy", 2, 2);
            addToBattlefield(PLAYER1_ID, createCreature("Vanilla", 2, 2));

            svc.checkAllyNontokenCreatureDeathTriggers(gd, PLAYER1_ID, dying);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Non-may effect adds to stack with sourcePermanentId")
        void nonMayEffect_addsToStackWithSourcePermanentId() {
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
        void mayEffect_queuesMayAbility() {
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
        void tokenCard_doesNothing() {
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
        void imprintTrigger_queuesMayAbilityWithDyingCardId() {
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
        void returnTrigger_requiresCardInGraveyard() {
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
        void returnTrigger_firesWhenInGraveyard() {
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
        void mayEffect_queuesMayAbility() {
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
}
