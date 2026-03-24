package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentLeavesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeathTriggerCollectorServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @InjectMocks
    private DeathTriggerCollectorService svc;

    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
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

    private Card createEquipment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        card.setManaCost("");
        return card;
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect rawEffect) {
        return new TriggerMatchContext(gd, perm, controllerId, rawEffect);
    }

    // ── ON_DEATH handlers ──────────────────────────────────────────────

    @Nested
    @DisplayName("handleDealDamageToBlockedAttackers")
    class DealDamageToBlockedAttackers {

        @Test
        @DisplayName("Triggers during combat when creature was blocking")
        void triggersDuringCombat() {
            Card card = createCreature("Blocker", 1, 1);
            var deathDmg = new DealDamageToBlockedAttackersOnDeathEffect(2);
            Permanent perm = new Permanent(card);
            UUID attackerId = UUID.randomUUID();
            perm.getBlockingTargetIds().add(attackerId);
            gd.currentStep = TurnStep.COMBAT_DAMAGE;
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            boolean result = svc.handleDealDamageToBlockedAttackers(match(perm, PLAYER1_ID, deathDmg), deathDmg, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTargetIds()).containsExactly(attackerId);
        }

        @Test
        @DisplayName("Does not trigger outside combat")
        void doesNotTriggerOutsideCombat() {
            Card card = createCreature("Blocker", 1, 1);
            var deathDmg = new DealDamageToBlockedAttackersOnDeathEffect(2);
            Permanent perm = new Permanent(card);
            perm.getBlockingTargetIds().add(UUID.randomUUID());
            gd.currentStep = TurnStep.PRECOMBAT_MAIN;
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            boolean result = svc.handleDealDamageToBlockedAttackers(match(perm, PLAYER1_ID, deathDmg), deathDmg, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not trigger when not blocking")
        void doesNotTriggerWhenNotBlocking() {
            Card card = createCreature("NonBlocker", 1, 1);
            var deathDmg = new DealDamageToBlockedAttackersOnDeathEffect(2);
            Permanent perm = new Permanent(card);
            gd.currentStep = TurnStep.COMBAT_DAMAGE;
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            assertThat(svc.handleDealDamageToBlockedAttackers(match(perm, PLAYER1_ID, deathDmg), deathDmg, ctx)).isFalse();
        }

        @Test
        @DisplayName("Does not trigger when dyingPermanent is null")
        void doesNotTriggerWhenPermanentNull() {
            Card card = createCreature("Ghost", 1, 1);
            var deathDmg = new DealDamageToBlockedAttackersOnDeathEffect(2);
            Permanent perm = new Permanent(card);
            gd.currentStep = TurnStep.COMBAT_DAMAGE;
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, null);

            assertThat(svc.handleDealDamageToBlockedAttackers(match(perm, PLAYER1_ID, deathDmg), deathDmg, ctx)).isFalse();
        }

        @Test
        @DisplayName("Does not trigger when step is null")
        void doesNotTriggerWhenStepNull() {
            Card card = createCreature("Blocker", 1, 1);
            var deathDmg = new DealDamageToBlockedAttackersOnDeathEffect(2);
            Permanent perm = new Permanent(card);
            perm.getBlockingTargetIds().add(UUID.randomUUID());
            gd.currentStep = null;
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            assertThat(svc.handleDealDamageToBlockedAttackers(match(perm, PLAYER1_ID, deathDmg), deathDmg, ctx)).isFalse();
        }
    }

    @Nested
    @DisplayName("handleDeathMayPayMana")
    class DeathMayPayMana {

        @Test
        @DisplayName("Queues may ability with mana cost")
        void queuesMayAbility() {
            Card card = createCreature("Mana Dude", 2, 2);
            var mayPay = new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay 2?");
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            assertThat(svc.handleDeathMayPayMana(match(perm, PLAYER1_ID, mayPay), mayPay, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayPayManaEffect.class);
        }
    }

    @Nested
    @DisplayName("handleLosesLifeEqualToPower")
    class LosesLifeEqualToPower {

        @Test
        @DisplayName("Bakes effective power from permanent")
        void bakesPowerFromPermanent() {
            Card card = createCreature("Vengeful", 4, 3);
            var effect = new TargetPlayerLosesLifeEqualToPowerEffect();
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleLosesLifeEqualToPower(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (TargetPlayerLosesLifeEffect) gd.pendingDeathTriggerTargets.peek().effects().get(0);
            assertThat(resolved.amount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Falls back to card power when no permanent")
        void fallsBackToCardPower() {
            Card card = createCreature("Ghost", 3, 2);
            var effect = new TargetPlayerLosesLifeEqualToPowerEffect();
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, null);

            svc.handleLosesLifeEqualToPower(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (TargetPlayerLosesLifeEffect) gd.pendingDeathTriggerTargets.peek().effects().get(0);
            assertThat(resolved.amount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Clamps negative power to 0")
        void clampsNegativePower() {
            Card card = createCreature("Weakened", 2, 2);
            var effect = new TargetPlayerLosesLifeEqualToPowerEffect();
            Permanent perm = new Permanent(card);
            perm.setPowerModifier(-5);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleLosesLifeEqualToPower(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (TargetPlayerLosesLifeEffect) gd.pendingDeathTriggerTargets.peek().effects().get(0);
            assertThat(resolved.amount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Falls back to 0 when card power is null and no permanent")
        void nullCardPowerResolvesToZero() {
            Card card = createCreature("Powerless", 0, 2);
            card.setPower(null);
            var effect = new TargetPlayerLosesLifeEqualToPowerEffect();
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, null);

            svc.handleLosesLifeEqualToPower(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (TargetPlayerLosesLifeEffect) gd.pendingDeathTriggerTargets.peek().effects().get(0);
            assertThat(resolved.amount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("handleDeathMayEffect")
    class DeathMayEffect {

        @Test
        @DisplayName("Non-targeting MayEffect queues may ability")
        void nonTargetingQueuesMayAbility() {
            Card card = createCreature("Optional", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleDeathMayEffect(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("Targeting MayEffect goes to pendingDeathTriggerTargets (CR 603.3d)")
        void targetingGoesToPendingTargets() {
            Card card = createCreature("Targeted May", 2, 2);
            var may = new MayEffect(new TargetPlayerLosesLifeEffect(3), "Drain?");
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleDeathMayEffect(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleDeathDefault")
    class DeathDefault {

        @Test
        @DisplayName("Non-targeting effect adds to stack")
        void nonTargetingAddsToStack() {
            Card card = createCreature("Dying Dude", 2, 2);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(PLAYER1_ID);
            assertThat(entry.getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("Targeting effect queues DeathTriggerTarget")
        void targetingQueuesDeathTriggerTarget() {
            Card card = createCreature("Targeting Dude", 3, 3);
            var effect = new PutChargeCounterOnTargetPermanentEffect();
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
        }
    }

    // ── ON_ALLY_CREATURE_DIES handlers ─────────────────────────────────

    @Nested
    @DisplayName("handleAllyCreatureMayPay")
    class AllyCreatureMayPay {

        @Test
        @DisplayName("Queues may ability for MayPayManaEffect")
        void queuesMayAbility() {
            Card watcher = createCreature("Pay Watcher", 1, 1);
            var mayPay = new MayPayManaEffect("{1}", new DrawCardEffect(1), "Pay?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID);

            svc.handleAllyCreatureMayPay(match(perm, PLAYER1_ID, mayPay), mayPay, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayPayManaEffect.class);
        }
    }

    @Nested
    @DisplayName("handleAllyCreatureMay")
    class AllyCreatureMay {

        @Test
        @DisplayName("Queues may ability with sourcePermanentId")
        void queuesMayAbilityWithSourcePermanentId() {
            Card watcher = createCreature("May Watcher", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID);

            svc.handleAllyCreatureMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    // ── ON_EQUIPPED_CREATURE_DIES handler ──────────────────────────────

    @Nested
    @DisplayName("handleEquippedCreatureDeathDefault")
    class EquippedCreatureDeathDefault {

        @Test
        @DisplayName("Non-targeting effect adds to stack")
        void nonTargetingAddsToStack() {
            Card equipment = createEquipment("Death Sword");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getCard()).isEqualTo(equipment);
        }

        @Test
        @DisplayName("Targeting effect queues DeathTriggerTarget")
        void targetingQueuesDeathTriggerTarget() {
            Card equipment = createEquipment("Target Sword");
            var effect = new PutChargeCounterOnTargetPermanentEffect();
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card equipment = createEquipment("Trigger Blade");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Trigger Blade") && msg.contains("equipped creature died")));
        }
    }

    // ── ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD handlers ─────────────

    @Nested
    @DisplayName("handleReturnSourceAura")
    class ReturnSourceAura {

        @Test
        @DisplayName("Bakes dying creature's controller ID")
        void bakesControllerId() {
            Card aura = createEnchantment("Necrotic Plague");
            var effect = new ReturnSourceAuraToOpponentCreatureOnDeathEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, null);

            svc.handleReturnSourceAura(match(perm, PLAYER2_ID, effect), effect, ctx);

            var resolved = (ReturnSourceAuraToOpponentCreatureOnDeathEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.enchantedCreatureControllerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Does not bake controller when null")
        void doesNotBakeWhenNull() {
            Card aura = createEnchantment("Necrotic Plague");
            var effect = new ReturnSourceAuraToOpponentCreatureOnDeathEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null);

            svc.handleReturnSourceAura(match(perm, PLAYER2_ID, effect), effect, ctx);

            var resolved = (ReturnSourceAuraToOpponentCreatureOnDeathEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.enchantedCreatureControllerId()).isNull();
        }
    }

    @Nested
    @DisplayName("handleReturnEnchantedCreature")
    class ReturnEnchantedCreature {

        @Test
        @DisplayName("Bakes dying creature's card ID")
        void bakesCardId() {
            Card aura = createEnchantment("Demonic Vigor");
            var effect = new ReturnEnchantedCreatureToOwnerHandOnDeathEffect();
            Permanent perm = new Permanent(aura);
            UUID creatureCardId = UUID.randomUUID();
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, creatureCardId);

            svc.handleReturnEnchantedCreature(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (ReturnEnchantedCreatureToOwnerHandOnDeathEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.dyingCreatureCardId()).isEqualTo(creatureCardId);
        }

        @Test
        @DisplayName("Does not bake card ID when null")
        void doesNotBakeWhenNull() {
            Card aura = createEnchantment("Demonic Vigor");
            var effect = new ReturnEnchantedCreatureToOwnerHandOnDeathEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, null);

            svc.handleReturnEnchantedCreature(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (ReturnEnchantedCreatureToOwnerHandOnDeathEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.dyingCreatureCardId()).isNull();
        }
    }

    @Nested
    @DisplayName("handleEnchantedPermanentDeathDefault")
    class EnchantedPermanentDeathDefault {

        @Test
        @DisplayName("Adds to stack and logs")
        void addsToStackAndLogs() {
            Card aura = createEnchantment("Death Aura");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getCard()).isEqualTo(aura);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("enchanted permanent put into graveyard")));
        }
    }

    // ── ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD handlers ─────────────

    @Nested
    @DisplayName("handleEnchantedPermanentLeavesConditional")
    class EnchantedPermanentLeavesConditional {

        @Test
        @DisplayName("Fires when filter matches")
        void firesWhenFilterMatches() {
            Card aura = createEnchantment("Conditional Aura");
            var filter = new CardTypePredicate(CardType.CREATURE);
            var conditional = new EnchantedPermanentLeavesConditionalEffect(filter, List.of(new DrawCardEffect(2)));
            Card creature = createCreature("Leaving", 2, 2);
            Permanent leavingPerm = new Permanent(creature);
            Permanent auraPerm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPerm);

            when(gameQueryService.matchesCardPredicate(creature, filter, null)).thenReturn(true);

            assertThat(svc.handleEnchantedPermanentLeavesConditional(match(auraPerm, PLAYER1_ID, conditional), conditional, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve()).hasSize(1);
        }

        @Test
        @DisplayName("Does not fire when filter does not match")
        void doesNotFireWhenFilterDoesNotMatch() {
            Card aura = createEnchantment("Conditional Aura");
            var filter = new CardTypePredicate(CardType.CREATURE);
            var conditional = new EnchantedPermanentLeavesConditionalEffect(filter, List.of(new DrawCardEffect(2)));
            Card artifact = createArtifact("Leaving Artifact");
            Permanent leavingPerm = new Permanent(artifact);
            Permanent auraPerm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPerm);

            when(gameQueryService.matchesCardPredicate(artifact, filter, null)).thenReturn(false);

            assertThat(svc.handleEnchantedPermanentLeavesConditional(match(auraPerm, PLAYER1_ID, conditional), conditional, ctx)).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Fires unconditionally when filter is null")
        void firesWhenFilterNull() {
            Card aura = createEnchantment("Open Aura");
            var conditional = new EnchantedPermanentLeavesConditionalEffect(null, List.of(new DrawCardEffect(1)));
            Permanent leavingPerm = new Permanent(createCreature("Any", 2, 2));
            Permanent auraPerm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPerm);

            assertThat(svc.handleEnchantedPermanentLeavesConditional(match(auraPerm, PLAYER1_ID, conditional), conditional, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
        }
    }

    @Nested
    @DisplayName("handleEnchantedPermanentLeavesDefault")
    class EnchantedPermanentLeavesDefault {

        @Test
        @DisplayName("Non-conditional effect fires unconditionally")
        void firesUnconditionally() {
            Card aura = createEnchantment("LTB Aura");
            var effect = new DrawCardEffect(1);
            Permanent auraPerm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentLeaves(new Permanent(createCreature("Leaving", 2, 2)));

            svc.handleEnchantedPermanentLeavesDefault(match(auraPerm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("enchanted permanent left the battlefield")));
        }
    }

    // ── ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD handlers ───

    @Nested
    @DisplayName("Artifact graveyard handlers")
    class ArtifactGraveyardHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createArtifact("Optional Watcher");
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            svc.handleArtifactGraveyardMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(MayEffect.class);
        }

        @Test
        @DisplayName("DealDamage effect sets target to artifact controller")
        void dealDamageSetsTarget() {
            Card watcher = createArtifact("Damage Watcher");
            var effect = new DealDamageToTriggeringPermanentControllerEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleArtifactGraveyardDamageController(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Default effect has null targetId and sets sourcePermanentId")
        void defaultSetsSourcePermanentId() {
            Card watcher = createArtifact("Plain Watcher");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            svc.handleArtifactGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getTargetId()).isNull();
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("Opponent artifact graveyard handlers")
    class OpponentArtifactGraveyardHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createArtifact("Opponent May Watcher");
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleOpponentArtifactGraveyardMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default sets sourcePermanentId")
        void defaultSetsSourcePermanentId() {
            Card watcher = createArtifact("Opponent Tracker");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER2_ID, PLAYER2_ID);

            svc.handleOpponentArtifactGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    // ── ON_ANY_CREATURE_DIES handlers ──────────────────────────────────

    @Nested
    @DisplayName("Any creature death handlers")
    class AnyCreatureDeathHandlers {

        @Test
        @DisplayName("PutCountersOnSource sets sourcePermanentId")
        void putCountersSetsSourcePermanentId() {
            Card watcher = createCreature("Growing Watcher", 1, 1);
            var effect = new PutCountersOnSourceEffect(1, 1, 1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID);

            svc.handleAnyCreatureDeathPutCounters(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("Optional Watcher", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID);

            svc.handleAnyCreatureDeathMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default non-targeting adds to stack without sourcePermanentId")
        void defaultNonTargetingAddsToStack() {
            Card watcher = createCreature("Death Counter", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID);

            svc.handleAnyCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isNull();
        }

        @Test
        @DisplayName("Default targeting queues DeathTriggerTarget")
        void defaultTargetingQueuesDeathTriggerTarget() {
            Card watcher = createCreature("Target Watcher", 1, 1);
            var effect = new PutChargeCounterOnTargetPermanentEffect();
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID);

            svc.handleAnyCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingDeathTriggerTargets).hasSize(1);
        }
    }

    // ── ON_ALLY_NONTOKEN_CREATURE_DIES handlers ────────────────────────

    @Nested
    @DisplayName("Ally nontoken creature death handlers")
    class AllyNontokenCreatureDeathHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("May Ally", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID);

            svc.handleAllyNontokenMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default adds to stack with sourcePermanentId")
        void defaultAddsToStackWithSourcePermanentId() {
            Card watcher = createCreature("Ally Tracker", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID);

            svc.handleAllyNontokenDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    // ── ON_ANY_NONTOKEN_CREATURE_DIES handlers ─────────────────────────

    @Nested
    @DisplayName("Any nontoken creature death handlers")
    class AnyNontokenCreatureDeathHandlers {

        @Test
        @DisplayName("Imprint bakes dying card ID")
        void imprintBakesDyingCardId() {
            Card dying = createCreature("Dying Nontoken", 3, 3);
            Card watcher = createCreature("Mimic Vat", 0, 0);
            var imprint = new ImprintDyingCreatureEffect();
            var rawMay = new MayEffect(imprint, "Exile and imprint?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID);

            svc.handleImprintDyingCreature(match(perm, PLAYER1_ID, rawMay), imprint, ctx);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            var bakedImprint = (ImprintDyingCreatureEffect) gd.pendingMayAbilities.get(0).effects().get(0);
            assertThat(bakedImprint.dyingCardId()).isEqualTo(dying.getId());
        }

        @Test
        @DisplayName("ReturnDyingCreature fires when dying card is in controller's graveyard")
        void returnTriggerFiresWhenInGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var returnEffect = new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect();
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            gd.playerGraveyards.get(PLAYER1_ID).add(dying);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID);

            assertThat(svc.handleReturnDyingCreatureMayPay(match(perm, PLAYER1_ID, rawMayPay), returnEffect, ctx)).isTrue();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.get(0).manaCost()).isEqualTo("{4}");
        }

        @Test
        @DisplayName("ReturnDyingCreature does not fire when dying card is NOT in graveyard")
        void returnTriggerDoesNotFireWhenNotInGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var returnEffect = new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect();
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID);

            assertThat(svc.handleReturnDyingCreatureMayPay(match(perm, PLAYER1_ID, rawMayPay), returnEffect, ctx)).isFalse();
        }

        @Test
        @DisplayName("ReturnDyingCreature does not fire when graveyard is null")
        void returnTriggerNullGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var returnEffect = new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect();
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            gd.playerGraveyards.remove(PLAYER1_ID);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID);

            assertThat(svc.handleReturnDyingCreatureMayPay(match(perm, PLAYER1_ID, rawMayPay), returnEffect, ctx)).isFalse();
        }
    }

    // ── ON_OPPONENT_CREATURE_DIES handlers ─────────────────────────────

    @Nested
    @DisplayName("Opponent creature death handlers")
    class OpponentCreatureDeathHandlers {

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("Optional Vulture", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID);

            svc.handleOpponentCreatureDeathMay(match(perm, PLAYER2_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default sets targetId to dyingCreatureControllerId and sourcePermanentId")
        void defaultSetsTargetAndSourcePermanentId() {
            Card watcher = createCreature("Vulture", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID);

            svc.handleOpponentCreatureDeathDefault(match(perm, PLAYER2_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER1_ID);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    // ── ON_SELF_LEAVES_BATTLEFIELD handlers ────────────────────────────

    @Nested
    @DisplayName("Self leaves battlefield handlers")
    class SelfLeavesHandlers {

        @Test
        @DisplayName("ControllerLosesGameOnLeavesEffect converts to TargetPlayerLosesGameEffect")
        void controllerLosesGameConverts() {
            Card card = createCreature("Pact Creature", 5, 5);
            var effect = new ControllerLosesGameOnLeavesEffect();
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfLeaves(PLAYER1_ID);

            svc.handleControllerLosesGameOnLeaves(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var resolved = (TargetPlayerLosesGameEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.playerId()).isEqualTo(PLAYER1_ID);
        }

        @Test
        @DisplayName("Generic effect adds to stack")
        void genericEffectAddsToStack() {
            Card card = createCreature("Leaving Dude", 2, 2);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfLeaves(PLAYER1_ID);

            svc.handleSelfLeavesDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve().get(0)).isInstanceOf(DrawCardEffect.class);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card card = createCreature("Logged Leaver", 2, 2);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfLeaves(PLAYER1_ID);

            svc.handleSelfLeavesDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Logged Leaver") && msg.contains("left the battlefield")));
        }
    }

    // ── ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD handler ──

    @Nested
    @DisplayName("handleRegisterDelayedReturn")
    class RegisterDelayedReturn {

        @Test
        @DisplayName("Queues MayEffect with baked dying card ID")
        void queuesMayEffectWithBakedCardId() {
            Card dyingAura = createEnchantment("Dying Aura");
            Card tiana = createCreature("Tiana", 3, 3);
            var effect = new RegisterDelayedReturnCardFromGraveyardToHandEffect(null);
            Permanent perm = new Permanent(tiana);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingAura, PLAYER1_ID);

            svc.handleRegisterDelayedReturn(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            MayEffect may = (MayEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            var delayed = (RegisterDelayedReturnCardFromGraveyardToHandEffect) may.wrapped();
            assertThat(delayed.cardId()).isEqualTo(dyingAura.getId());
        }

        @Test
        @DisplayName("May prompt includes dying card name")
        void promptIncludesDyingCardName() {
            Card dyingEquip = createEquipment("Lost Sword");
            Card tiana = createCreature("Tiana", 3, 3);
            var effect = new RegisterDelayedReturnCardFromGraveyardToHandEffect(null);
            Permanent perm = new Permanent(tiana);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingEquip, PLAYER1_ID);

            svc.handleRegisterDelayedReturn(match(perm, PLAYER1_ID, effect), effect, ctx);

            MayEffect may = (MayEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(may.prompt()).contains("Lost Sword");
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card dyingAura = createEnchantment("Fallen Aura");
            Card tiana = createCreature("Tiana Ship's Caretaker", 3, 3);
            var effect = new RegisterDelayedReturnCardFromGraveyardToHandEffect(null);
            Permanent perm = new Permanent(tiana);
            var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingAura, PLAYER1_ID);

            svc.handleRegisterDelayedReturn(match(perm, PLAYER1_ID, effect), effect, ctx);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("Tiana Ship's Caretaker") && msg.contains("Fallen Aura")));
        }
    }
}
