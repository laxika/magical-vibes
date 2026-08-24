package com.github.laxika.magicalvibes.service.trigger;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerSacrificesCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentLeavesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndDrawEqualToDyingPowerEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToDyingPowerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class DeathTriggerCollectorServiceTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport graveyardTargetingSupport;

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
    @DisplayName("handleDistributeCountersAmongCreaturesOnDeath")
    class DistributeCountersAmongCreaturesOnDeath {

        @Test
        @DisplayName("Snapshots counter count and stacks a MayEffect")
        void snapshotsAndStacksMay() {
            Card card = createCreature("Hydra", 0, 0);
            var effect = DistributeCountersAmongCreaturesOnDeathEffect
                    .fromDyingSourceCountersAmongControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE);
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            assertThat(svc.handleDistributeCountersAmongCreaturesOnDeath(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();
            assertThat(gd.stack).hasSize(1);
            MayEffect may = (MayEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            var baked = (DistributeCountersAmongCreaturesOnDeathEffect) may.wrapped();
            assertThat(baked.count()).isEqualTo(3);
            assertThat(baked.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
        }

        @Test
        @DisplayName("Does not trigger when dying permanent is null")
        void noPermanent() {
            Card card = createCreature("Hydra", 0, 0);
            var effect = DistributeCountersAmongCreaturesOnDeathEffect
                    .fromDyingSourceCountersAmongControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE);
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, null);

            assertThat(svc.handleDistributeCountersAmongCreaturesOnDeath(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handlePutCounterOnTargetForEachDyingSourceCounter")
    class PutCounterOnTargetForEachDyingSourceCounter {

        @Test
        @DisplayName("Snapshots counters and preserves the target predicate")
        void snapshotsCountersAndPreservesTargetPredicate() {
            Card card = createCreature("Arcbound Bruiser", 0, 0);
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
            var targetPredicate = new PermanentAllOfPredicate(List.of(
                    new PermanentIsArtifactPredicate(),
                    new PermanentIsCreaturePredicate()
            ));
            var effect = new PutCounterOnTargetForEachDyingSourceCounterEffect(
                    CounterType.PLUS_ONE_PLUS_ONE, true, targetPredicate);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            assertThat(svc.handlePutCounterOnTargetForEachDyingSourceCounter(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            MayEffect may = (MayEffect) gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)
                    .effects().getFirst();
            var baked = (PutCounterOnTargetForEachDyingSourceCounterEffect) may.wrapped();
            assertThat(baked.count()).isEqualTo(3);
            assertThat(baked.targetPredicate()).isEqualTo(targetPredicate);
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

            var resolved = (LoseLifeEffect) gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class).effects().get(0);
            assertThat(resolved.amount()).isEqualTo(new Fixed(4));
        }

        @Test
        @DisplayName("Falls back to card power when no permanent")
        void fallsBackToCardPower() {
            Card card = createCreature("Ghost", 3, 2);
            var effect = new TargetPlayerLosesLifeEqualToPowerEffect();
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, null);

            svc.handleLosesLifeEqualToPower(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (LoseLifeEffect) gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class).effects().get(0);
            assertThat(resolved.amount()).isEqualTo(new Fixed(3));
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

            var resolved = (LoseLifeEffect) gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class).effects().get(0);
            assertThat(resolved.amount()).isEqualTo(new Fixed(0));
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

            var resolved = (LoseLifeEffect) gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class).effects().get(0);
            assertThat(resolved.amount()).isEqualTo(new Fixed(0));
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
        @DisplayName("Targeting MayEffect queues a DeathTriggerTarget interaction (CR 603.3d)")
        void targetingGoesToPendingTargets() {
            Card card = createCreature("Targeted May", 2, 2);
            var may = new MayEffect(new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER), "Drain?");
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleDeathMayEffect(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
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
            var effect = new PutCounterOnTargetPermanentEffect(CounterType.CHARGE);
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("Graveyard-targeting effect queues DeathTriggerTarget (Ruin Rat)")
        void graveyardTargetingQueuesDeathTriggerTarget() {
            Card card = createCreature("Ruin Rat", 1, 1);
            var effect = new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD);
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);

            svc.handleDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("Multi-target graveyard return queues DeathTriggerTarget")
        void multiTargetGraveyardReturnQueuesDeathTriggerTarget() {
            Card card = createCreature("Forked-Branch Garami", 4, 4);
            var effect = new ReturnTargetCardsFromGraveyardToHandEffect(null, 2);
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfDeath(card, PLAYER1_ID, true, perm);
            when(graveyardTargetingSupport.findTarget(List.of(effect))).thenReturn(
                    new com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport.Target(
                            null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, "to your hand", 2));

            svc.handleDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
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
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

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
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

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
        @DisplayName("Snapshots dying power for the optional life payment and draw")
        void snapshotsDyingPowerForPaymentAndDraw() {
            Card equipment = createEquipment("Mask");
            var effect = new MayPayLifeAndDrawEqualToDyingPowerEffect();
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null, 4);

            svc.handleEquippedCreatureDeathMayPayLifeAndDrawEqualToPower(
                    match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var resolved = (MayPayLifeEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.lifeCost()).isEqualTo(4);
            assertThat(resolved.wrapped()).isEqualTo(new DrawCardEffect(4));
        }

        @Test
        @DisplayName("Non-targeting effect adds to stack")
        void nonTargetingAddsToStack() {
            Card equipment = createEquipment("Death Sword");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getCard()).isEqualTo(equipment);
        }

        @Test
        @DisplayName("Targeting effect queues DeathTriggerTarget")
        void targetingQueuesDeathTriggerTarget() {
            Card equipment = createEquipment("Target Sword");
            var effect = new PutCounterOnTargetPermanentEffect(CounterType.CHARGE);
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card equipment = createEquipment("Trigger Blade");
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(equipment);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            svc.handleEquippedCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("Trigger Blade") && logEntry.plainText().contains("equipped creature died")));
        }
    }

    @Nested
    @DisplayName("handleEquippedCreatureReturn")
    class EquippedCreatureReturn {

        @Test
        @DisplayName("Binds the dying card and carries the source Equipment as the entry's target")
        void bindsDyingCardAndSource() {
            Card equipment = createEquipment("Oathkeeper");
            Card dying = createCreature("Samurai", 3, 3);
            Permanent perm = new Permanent(equipment);
            var effect = new ReturnDyingCreatureToBattlefieldEffect(false);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, dying);

            svc.handleEquippedCreatureReturn(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(perm.getId());
            var bound = (ReturnDyingCreatureToBattlefieldEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(bound.dyingCardId()).isEqualTo(dying.getId());
            assertThat(bound.attachSource()).isFalse();
        }

        @Test
        @DisplayName("Does not fire without a dying card")
        void noDyingCard() {
            Card equipment = createEquipment("Oathkeeper");
            Permanent perm = new Permanent(equipment);
            var effect = new ReturnDyingCreatureToBattlefieldEffect(false);
            var ctx = new TriggerContext.EquippedCreatureDeath(UUID.randomUUID(), PLAYER1_ID, null);

            assertThat(svc.handleEquippedCreatureReturn(match(perm, PLAYER1_ID, effect), effect, ctx)).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleExileEquippedCreature")
    class ExileEquippedCreature {

        @Test
        @DisplayName("Binds the Equipment's last-known attachment")
        void bindsLastKnownAttachment() {
            Card equipment = createEquipment("Oathkeeper");
            Permanent perm = new Permanent(equipment);
            UUID equippedId = UUID.randomUUID();
            perm.setAttachedTo(equippedId);
            var effect = new ExileEquippedCreatureEffect();
            var ctx = new TriggerContext.SelfDeath(equipment, PLAYER1_ID, false, perm);

            svc.handleExileEquippedCreature(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var bound = (ExileEquippedCreatureEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(bound.equippedCreatureId()).isEqualTo(equippedId);
        }

        @Test
        @DisplayName("Does not fire when the Equipment was unattached")
        void unattachedDoesNotFire() {
            Card equipment = createEquipment("Oathkeeper");
            Permanent perm = new Permanent(equipment);
            var effect = new ExileEquippedCreatureEffect();
            var ctx = new TriggerContext.SelfDeath(equipment, PLAYER1_ID, false, perm);

            assertThat(svc.handleExileEquippedCreature(match(perm, PLAYER1_ID, effect), effect, ctx)).isFalse();
            assertThat(gd.stack).isEmpty();
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
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, null, 0, 0);

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
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null, 0, 0);

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
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, creatureCardId, 0, 0);

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
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER1_ID, null, 0, 0);

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
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null, 0, 0);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getCard()).isEqualTo(aura);
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("enchanted permanent put into graveyard")));
        }

        @Test
        @DisplayName("Snapshots event value for a dynamic look count")
        void snapshotsEventValueForDynamicLookCount() {
            Card aura = createEnchantment("Necrosynthesis");
            var effect = new LookAtTopCardsEffect(
                    new EventValue(), new Fixed(1), null,
                    LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), null, null, 3, 3);

            svc.handleEnchantedPermanentDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("handleEnchantedCreatureControllerLosesLife")
    class EnchantedCreatureControllerLosesLife {

        @Test
        @DisplayName("Bakes the dying creature's toughness and controller into the effect")
        void bakesToughnessAndController() {
            Card aura = createEnchantment("Banewasp Affliction");
            var effect = new EnchantedCreatureControllerLosesLifeEffect(0);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, 0, 4);

            svc.handleEnchantedCreatureControllerLosesLife(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (EnchantedCreatureControllerLosesLifeEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.amount()).isEqualTo(4);
            assertThat(resolved.affectedPlayerId()).isEqualTo(PLAYER2_ID);
        }

        @Test
        @DisplayName("Clamps negative toughness to zero life loss")
        void clampsNegativeToughness() {
            Card aura = createEnchantment("Banewasp Affliction");
            var effect = new EnchantedCreatureControllerLosesLifeEffect(0);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, 0, -1);

            svc.handleEnchantedCreatureControllerLosesLife(match(perm, PLAYER1_ID, effect), effect, ctx);

            var resolved = (EnchantedCreatureControllerLosesLifeEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.amount()).isZero();
        }
    }

    @Nested
    @DisplayName("handleEnchantedCreatureControllerDrawsCard")
    class EnchantedCreatureControllerDrawsCard {

        @Test
        @DisplayName("Bakes the dying creature's controller as the draw target")
        void bakesControllerId() {
            Card aura = createEnchantment("Fate Foretold");
            var effect = new DrawCardForTargetPlayerEffect(1);
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, 0, 0);

            svc.handleEnchantedCreatureControllerDrawsCard(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }
    }

    @Nested
    @DisplayName("handleEnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness")
    class EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness {

        @Test
        @DisplayName("Bakes power loss and toughness gain onto one stack entry")
        void bakesPowerLossAndToughnessGain() {
            Card aura = createEnchantment("Death Watch");
            var effect = new EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, 2, 4);

            svc.handleEnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness(
                    match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var effects = gd.stack.get(0).getEffectsToResolve();
            assertThat(effects).hasSize(2);
            var loss = (EnchantedCreatureControllerLosesLifeEffect) effects.get(0);
            assertThat(loss.amount()).isEqualTo(2);
            assertThat(loss.affectedPlayerId()).isEqualTo(PLAYER2_ID);
            var gain = (GainLifeEffect) effects.get(1);
            assertThat(gain.amount()).isEqualTo(new Fixed(4));
        }

        @Test
        @DisplayName("Clamps negative power and toughness to zero")
        void clampsNegatives() {
            Card aura = createEnchantment("Death Watch");
            var effect = new EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect();
            Permanent perm = new Permanent(aura);
            var ctx = new TriggerContext.EnchantedPermanentDeath(UUID.randomUUID(), PLAYER2_ID, null, -3, -1);

            svc.handleEnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness(
                    match(perm, PLAYER1_ID, effect), effect, ctx);

            var effects = gd.stack.get(0).getEffectsToResolve();
            assertThat(((EnchantedCreatureControllerLosesLifeEffect) effects.get(0)).amount()).isZero();
            assertThat(((GainLifeEffect) effects.get(1)).amount()).isEqualTo(new Fixed(0));
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
            var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPerm, PLAYER1_ID);

            when(predicateEvaluationService.matchesCardPredicate(creature, filter, null)).thenReturn(true);

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
            var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPerm, PLAYER1_ID);

            when(predicateEvaluationService.matchesCardPredicate(artifact, filter, null)).thenReturn(false);

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
            var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPerm, PLAYER1_ID);

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
            var ctx = new TriggerContext.EnchantedPermanentLeaves(new Permanent(createCreature("Leaving", 2, 2)), PLAYER1_ID);

            svc.handleEnchantedPermanentLeavesDefault(match(auraPerm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("enchanted permanent left the battlefield")));
        }
    }

    @Nested
    @DisplayName("handleEnchantedControllerSacrificesCreatureOnLeave")
    class EnchantedControllerSacrificesCreatureOnLeave {

        @Test
        @DisplayName("Bakes the leaving creature's controller, not the Aura controller")
        void bakesLeavingControllerNotAuraController() {
            Card aura = createEnchantment("Funeral March");
            var effect = new EnchantedControllerSacrificesCreatureOnLeaveEffect();
            Permanent auraPerm = new Permanent(aura);
            // Aura controlled by PLAYER1, but the enchanted creature was controlled by PLAYER2.
            var ctx = new TriggerContext.EnchantedPermanentLeaves(
                    new Permanent(createCreature("Leaving", 2, 2)), PLAYER2_ID);

            assertThat(svc.handleEnchantedControllerSacrificesCreatureOnLeave(
                    match(auraPerm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            assertThat(gd.stack).hasSize(1);
            var resolved = (EnchantedControllerSacrificesCreatureOnLeaveEffect)
                    gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(resolved.enchantedControllerId()).isEqualTo(PLAYER2_ID);
        }
    }

    @Nested
    @DisplayName("Any permanent graveyard handlers")
    class AnyPermanentGraveyardHandlers {

        @Test
        @DisplayName("Default effect queues a trigger for any permanent, including tokens")
        void defaultQueuesTrigger() {
            Card watcher = createEnchantment("Last Laugh");
            Permanent perm = new Permanent(watcher);
            var effect = new MassDamageEffect(1, true);
            var dying = createArtifact("Treasure");
            dying.setToken(true);
            var ctx = new TriggerContext.AnyPermanentGraveyard(dying, PLAYER2_ID, PLAYER2_ID);

            assertThat(svc.handleAnyPermanentGraveyardDefault(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.get(0).getTargetId()).isEqualTo(PLAYER2_ID);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
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
            var effect = new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER);
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

        @Test
        @DisplayName("Controller conditional queues a player-targeted trigger with source snapshot")
        void controllerConditionalQueuesTargetedTrigger() {
            Card watcher = createCreature("Marionette Master", 1, 3);
            Permanent perm = new Permanent(watcher);
            var effect = new TriggeringArtifactControllerConditionalEffect(
                    new LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER));
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER1_ID);

            assertThat(svc.handleArtifactGraveyardControllerConditional(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isTrue();

            var pending = (PermanentChoiceContext.SpellTargetTriggerAnyTarget)
                    gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.playerTargetOnly()).isTrue();
            assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
            assertThat(pending.sourcePermanentSnapshot()).isNotNull();
            assertThat(pending.effects()).containsExactly(effect.wrapped());
        }

        @Test
        @DisplayName("Controller conditional does not fire for an opponent's artifact")
        void controllerConditionalDoesNotFireForOpponentArtifact() {
            Card watcher = createCreature("Marionette Master", 1, 3);
            Permanent perm = new Permanent(watcher);
            var effect = new TriggeringArtifactControllerConditionalEffect(
                    new LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER));
            var ctx = new TriggerContext.ArtifactGraveyard(PLAYER1_ID, PLAYER2_ID);

            assertThat(svc.handleArtifactGraveyardControllerConditional(
                    match(perm, PLAYER1_ID, effect), effect, ctx)).isFalse();
            assertThat(gd.pendingInteractions).isEmpty();
        }
    }

    @Nested
    @DisplayName("Enchantment graveyard handlers")
    class EnchantmentGraveyardHandlers {

        @Test
        @DisplayName("Default effect has null targetId and sets sourcePermanentId")
        void defaultSetsSourcePermanentId() {
            Card watcher = createCreature("Femeref Enchantress", 1, 2);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.EnchantmentGraveyard(PLAYER1_ID, PLAYER1_ID);

            svc.handleEnchantmentGraveyardDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

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
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathPutCounters(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("BoostSelfEffect sets sourcePermanentId")
        void boostSelfSetsSourcePermanentId() {
            Card watcher = createCreature("Gristle Grinner", 3, 3);
            var effect = new BoostSelfEffect(2, 2);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathBoostSelf(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("TapPermanentsEffect sets sourcePermanentId")
        void tapSetsSourcePermanentId() {
            Card watcher = createCreature("Fleshmad Steed", 2, 2);
            var effect = new TapPermanentsEffect(TapUntapScope.SELF);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathTap(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("MayEffect queues may ability")
        void mayEffectQueuesMayAbility() {
            Card watcher = createCreature("Optional Watcher", 1, 1);
            var may = new MayEffect(new DrawCardEffect(1), "Draw?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("PutCountersEqualToDyingPower bakes the dying power into an optional counter ability")
        void putCountersEqualToDyingPowerBakesPower() {
            Card watcher = createCreature("Kresh the Bloodbraided", 3, 3);
            var effect = new PutCountersOnSourceEqualToDyingPowerEffect(1, 1, true);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 4, 4), PLAYER1_ID, 4, 4);

            svc.handleAnyCreatureDeathPutCountersEqualToPower(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
            var may = (MayEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            var counters = (PutCountersOnSourceEffect) may.wrapped();
            assertThat(counters.amount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Targeting MayEffect queues a DeathTriggerTarget interaction (CR 603.3d)")
        void targetingMayQueuesDeathTriggerTarget() {
            Card watcher = createEnchantment("Vicious Shadows");
            var may = new MayEffect(
                    new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER), "Deal damage?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("Target player damage queues a death-trigger target choice")
        void targetPlayerDamageQueuesDeathTriggerTarget() {
            Card watcher = createCreature("Rage Thrower", 4, 2);
            var effect = new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER2_ID, 1, 1);

            svc.handleAnyCreatureDeathDamageController(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
        }

        @Test
        @DisplayName("BecomeCopyOfDyingCreature queues a pay-mana may ability with the dying card baked in")
        void becomeCopyQueuesMayPayAbility() {
            Card puca = createCreature("Cemetery Puca", 1, 2);
            Card dying = createCreature("Dead Creature", 2, 2);
            var copyEffect = new BecomeCopyOfDyingCreatureEffect();
            var rawMayPay = new MayPayManaEffect("{1}", copyEffect, "Pay {1}?");
            Permanent perm = new Permanent(puca);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            assertThat(svc.handleAnyCreatureDeathBecomeCopy(match(perm, PLAYER1_ID, rawMayPay), copyEffect, ctx)).isTrue();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.get(0).manaCost()).isEqualTo("{1}");
            var baked = (BecomeCopyOfDyingCreatureEffect) gd.pendingMayAbilities.get(0).effects().get(0);
            assertThat(baked.dyingCardId()).isEqualTo(dying.getId());
        }

        @Test
        @DisplayName("BecomeCopyOfDyingCreature does not fire when the dying card is null")
        void becomeCopyNoFireWithoutDyingCard() {
            Card puca = createCreature("Cemetery Puca", 1, 2);
            var copyEffect = new BecomeCopyOfDyingCreatureEffect();
            var rawMayPay = new MayPayManaEffect("{1}", copyEffect, "Pay {1}?");
            Permanent perm = new Permanent(puca);
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID, 0, 0);

            assertThat(svc.handleAnyCreatureDeathBecomeCopy(match(perm, PLAYER1_ID, rawMayPay), copyEffect, ctx)).isFalse();
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Default non-targeting adds to stack without sourcePermanentId")
        void defaultNonTargetingAddsToStack() {
            Card watcher = createCreature("Death Counter", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isNull();
        }

        @Test
        @DisplayName("Default targeting queues DeathTriggerTarget")
        void defaultTargetingQueuesDeathTriggerTarget() {
            Card watcher = createCreature("Target Watcher", 1, 1);
            var effect = new PutCounterOnTargetPermanentEffect(CounterType.CHARGE);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 1, 1), PLAYER1_ID, 1, 1);

            svc.handleAnyCreatureDeathDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.pendingInteractions).filteredOn(PermanentChoiceContext.DeathTriggerTarget.class::isInstance).hasSize(1);
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
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

            svc.handleAllyNontokenMay(match(perm, PLAYER1_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default adds to stack with sourcePermanentId")
        void defaultAddsToStackWithSourcePermanentId() {
            Card watcher = createCreature("Ally Tracker", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(createCreature("Dying", 2, 2), PLAYER1_ID, 2, 2);

            svc.handleAllyNontokenDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.get(0).getSourcePermanentId()).isEqualTo(perm.getId());
        }

        @Test
        @DisplayName("MayPayManaEffect binds a dying-card-aware wrapped effect")
        void mayPayBindsDyingCardAwareWrappedEffect() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card watcher = createCreature("Decaying Soil", 0, 0);
            var returnEffect = new ReturnTriggeringCardToOwnerHandEffect();
            var mayPay = new MayPayManaEffect("{1}", returnEffect, "Pay 1?");
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            svc.handleAllyNontokenMayPay(match(perm, PLAYER1_ID, mayPay), mayPay, ctx);

            assertThat(gd.stack).hasSize(1);
            var queued = (MayPayManaEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            var bound = (ReturnTriggeringCardToOwnerHandEffect) queued.wrapped();
            assertThat(bound.dyingCardId()).isEqualTo(dying.getId());
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
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 3, 3);

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
            var returnEffect = new ReturnDyingCreatureToBattlefieldEffect(true);
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            gd.playerGraveyards.get(PLAYER1_ID).add(dying);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            assertThat(svc.handleReturnDyingCreatureMayPay(match(perm, PLAYER1_ID, rawMayPay), returnEffect, ctx)).isTrue();
            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.get(0).manaCost()).isEqualTo("{4}");
        }

        @Test
        @DisplayName("ReturnDyingCreature does not fire when dying card is NOT in graveyard")
        void returnTriggerDoesNotFireWhenNotInGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var returnEffect = new ReturnDyingCreatureToBattlefieldEffect(true);
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

            assertThat(svc.handleReturnDyingCreatureMayPay(match(perm, PLAYER1_ID, rawMayPay), returnEffect, ctx)).isFalse();
        }

        @Test
        @DisplayName("ReturnDyingCreature does not fire when graveyard is null")
        void returnTriggerNullGraveyard() {
            Card dying = createCreature("Dead Creature", 2, 2);
            Card deathmantle = createEquipment("Nim Deathmantle");
            var returnEffect = new ReturnDyingCreatureToBattlefieldEffect(true);
            var rawMayPay = new MayPayManaEffect("{4}", returnEffect, "Pay 4?");
            Permanent perm = new Permanent(deathmantle);
            gd.playerGraveyards.remove(PLAYER1_ID);
            var ctx = new TriggerContext.CreatureDeath(dying, PLAYER1_ID, 2, 2);

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
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID, 0, 0);

            svc.handleOpponentCreatureDeathMay(match(perm, PLAYER2_ID, may), may, ctx);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Default sets targetId to dyingCreatureControllerId and sourcePermanentId")
        void defaultSetsTargetAndSourcePermanentId() {
            Card watcher = createCreature("Vulture", 1, 1);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(watcher);
            var ctx = new TriggerContext.CreatureDeath(null, PLAYER1_ID, 0, 0);

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
        @DisplayName("DestroyEnchantedCreatureOnLeaveEffect captures the attached permanent ID")
        void destroyEnchantedCreatureCapturesAttachedPermanent() {
            Permanent aura = new Permanent(createEnchantment("Leaving Aura"));
            Permanent creature = new Permanent(createCreature("Enchanted Creature", 2, 2));
            aura.setAttachedTo(creature.getId());
            var effect = new DestroyEnchantedCreatureOnLeaveEffect();
            var ctx = new TriggerContext.SelfLeaves(PLAYER1_ID);

            svc.handleDestroyEnchantedCreatureOnLeave(match(aura, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var captured = (DestroyEnchantedCreatureOnLeaveEffect) gd.stack.get(0).getEffectsToResolve().get(0);
            assertThat(captured.enchantedPermanentId()).isEqualTo(creature.getId());
            assertThat(captured.cannotBeRegenerated()).isTrue();
        }

        @Test
        @DisplayName("ReturnAllCardsExiledWithSourceEffect preserves the leaving permanent ID")
        void returnAllCardsExiledWithSourcePreservesSourceId() {
            Permanent perm = new Permanent(createCreature("Leaving Angel", 6, 6));
            var effect = new ReturnAllCardsExiledWithSourceEffect();
            var ctx = new TriggerContext.SelfLeaves(PLAYER1_ID);

            svc.handleReturnAllCardsExiledWithSourceOnLeave(match(perm, PLAYER1_ID, effect), effect, ctx);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.get(0);
            assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("Logs trigger message")
        void logsMessage() {
            Card card = createCreature("Logged Leaver", 2, 2);
            var effect = new DrawCardEffect(1);
            Permanent perm = new Permanent(card);
            var ctx = new TriggerContext.SelfLeaves(PLAYER1_ID);

            svc.handleSelfLeavesDefault(match(perm, PLAYER1_ID, effect), effect, ctx);

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("Logged Leaver") && logEntry.plainText().contains("left the battlefield")));
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

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("Tiana Ship's Caretaker") && logEntry.plainText().contains("Fallen Aura")));
        }
    }
}
