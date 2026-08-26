package com.github.laxika.magicalvibes.service.trigger;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfDamagedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DamageTriggerCollectorServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private CreatureControlService creatureControlService;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService conditionEvaluationService;

    @InjectMocks
    private DamageTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    // ===== Helpers =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId,
            com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    @Test
    @DisplayName("queues life gain for damage from a controlled noncreature source")
    void queuesLifeGainForControlledNoncreatureSource() {
        Permanent tamanoa = createPermanent("Tamanoa");
        Card sourceCard = new Card();
        sourceCard.setName("Shock");
        sourceCard.setType(CardType.INSTANT);
        GainLifeEffect effect = new GainLifeEffect(new EventValue());
        var ctx = new TriggerContext.SourceDealsDamage(sourceCard, player1Id, 2,
                Map.of(player2Id, 2));

        boolean result = registry.dispatch(
                match(tamanoa, player1Id, effect), EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("does not queue life gain for damage from a creature source")
    void doesNotQueueLifeGainForCreatureSource() {
        Permanent tamanoa = createPermanent("Tamanoa");
        Card sourceCard = createCard("Grizzly Bears");
        GainLifeEffect effect = new GainLifeEffect(new EventValue());
        var ctx = new TriggerContext.SourceDealsDamage(sourceCard, player1Id, 2,
                Map.of(player2Id, 2));

        boolean result = registry.dispatch(
                match(tamanoa, player1Id, effect), EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, effect, ctx);

        assertThat(result).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("does not queue life gain for a noncreature source controlled by another player")
    void doesNotQueueLifeGainForAnotherPlayersSource() {
        Permanent tamanoa = createPermanent("Tamanoa");
        Card sourceCard = new Card();
        sourceCard.setName("Shock");
        sourceCard.setType(CardType.INSTANT);
        GainLifeEffect effect = new GainLifeEffect(new EventValue());
        var ctx = new TriggerContext.SourceDealsDamage(sourceCard, player2Id, 2,
                Map.of(player1Id, 2));

        boolean result = registry.dispatch(
                match(tamanoa, player1Id, effect), EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, effect, ctx);

        assertThat(result).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("uses the current type of a permanent damage source")
    void usesCurrentTypeOfPermanentSource() {
        Permanent tamanoa = createPermanent("Tamanoa");
        Permanent source = createPermanent("Animated Artifact");
        GainLifeEffect effect = new GainLifeEffect(new EventValue());
        var ctx = new TriggerContext.SourceDealsDamage(source.getCard(), player1Id, source.getId(), 2,
                Map.of(player2Id, 2));

        when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);
        when(gameQueryService.isCreature(gd, source)).thenReturn(false);

        boolean result = registry.dispatch(
                match(tamanoa, player1Id, effect), EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("queues a source-controller sacrifice trigger for opponent damage")
    void queuesSourceControllerSacrificeTrigger() {
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        Permanent watcher = createPermanent("Michiko Konda, Truth Seeker");
        var effect = new SacrificePermanentsEffect(1,
                new com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate(),
                SacrificeRecipient.TARGET_PLAYER);
        var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3, null, player2Id);

        boolean result = registry.dispatch(
                match(watcher, player1Id, effect),
                EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2Id);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("preserves a may wrapper for source damage to an opponent triggers")
    void preservesMayWrapperForAllySourceDamageToOpponent() {
        Permanent quest = createPermanent("Quest for Pure Flame");
        MayEffect effect = new MayEffect(
                new PutCountersOnSelfEffect(CounterType.QUEST),
                "Put a quest counter on Quest for Pure Flame?");
        var ctx = new TriggerContext.DamageToControllerAmount(player2Id, 2);

        boolean result = registry.dispatch(
                match(quest, player1Id, effect),
                EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("queues a may ability for combat damage to a creature")
    void queuesMayForAllyCombatDamageToCreature() {
        Permanent quest = createPermanent("Quest for the Gemblades");
        Permanent source = createPermanent("Grizzly Bears");
        Permanent damaged = createPermanent("Serra Angel");
        MayEffect effect = new MayEffect(
                new PutCountersOnSelfEffect(CounterType.QUEST),
                "Put a quest counter on Quest for the Gemblades?");
        var ctx = new TriggerContext.CreatureDealsDamageToCreature(source, damaged.getId(), 2, true);

        boolean result = registry.dispatch(
                match(quest, player1Id, effect),
                EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(quest.getId());
    }

    @Test
    @DisplayName("does not queue a may ability for noncombat damage to a creature")
    void doesNotQueueMayForNoncombatDamageToCreature() {
        Permanent quest = createPermanent("Quest for the Gemblades");
        Permanent source = createPermanent("Grizzly Bears");
        Permanent damaged = createPermanent("Serra Angel");
        MayEffect effect = new MayEffect(
                new PutCountersOnSelfEffect(CounterType.QUEST),
                "Put a quest counter on Quest for the Gemblades?");
        var ctx = new TriggerContext.CreatureDealsDamageToCreature(source, damaged.getId(), 2, false);

        boolean result = registry.dispatch(
                match(quest, player1Id, effect),
                EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE, effect, ctx);

        assertThat(result).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Nested
    @DisplayName("ON_ANY_CREATURE_DEALT_DAMAGE — permanent conditional")
    class AnyCreatureDealtDamageConditional {

        @Test
        @DisplayName("filters the damaged creature and queues the wrapped effect")
        void filtersDamagedCreatureAndQueuesWrappedEffect() {
            Permanent watcher = createPermanent("Rite of Passage");
            Permanent damaged = createPermanent("Hill Giant");
            var wrapped = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE);
            var effect = new TriggeringPermanentConditionalEffect(
                    new PermanentControlledBySourceControllerPredicate(), wrapped);
            var ctx = new TriggerContext.AnyCreatureDealtDamage(damaged, player1Id, 2);

            when(predicateEvaluationService.matchesPermanentPredicate(eq(damaged),
                    eq((PermanentPredicate) effect.predicate()), any(FilterContext.class))).thenReturn(true);

            boolean result = registry.dispatch(
                    match(watcher, player1Id, effect), EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(damaged.getId());
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(wrapped);
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        }

        @Test
        @DisplayName("does not queue when the damaged creature fails the predicate")
        void skipsNonMatchingDamagedCreature() {
            Permanent watcher = createPermanent("Rite of Passage");
            Permanent damaged = createPermanent("Hill Giant");
            var wrapped = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE);
            var effect = new TriggeringPermanentConditionalEffect(
                    new PermanentControlledBySourceControllerPredicate(), wrapped);
            var ctx = new TriggerContext.AnyCreatureDealtDamage(damaged, player2Id, 2);

            when(predicateEvaluationService.matchesPermanentPredicate(eq(damaged),
                    eq((PermanentPredicate) effect.predicate()), any(FilterContext.class))).thenReturn(false);

            boolean result = registry.dispatch(
                    match(watcher, player1Id, effect), EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("ON_ENCHANTED_CREATURE_DEALT_DAMAGE — CreateTokenEffect")
    class CreateTokensOnEnchantedCreatureDamage {

        @Test
        @DisplayName("queues a generic effect and records damage")
        void queuesGenericEffect() {
            Permanent aura = createPermanent("Soul Link");
            Permanent creature = createPermanent("Hill Giant");
            GainLifeEffect effect = new GainLifeEffect(new EventValue());
            var ctx = new TriggerContext.DamageToCreature(creature, 3, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1Id);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("queues tokens for the enchanted creature's current controller and records damage")
        void queuesTokensForEnchantedCreatureController() {
            Permanent aura = createPermanent("Druid's Call");
            Permanent creature = createPermanent("Hill Giant");
            CreateTokenEffect effect = new CreateTokenEffect(new EventValue(), "Squirrel", 1, 1,
                    com.github.laxika.magicalvibes.model.CardColor.GREEN,
                    java.util.List.of(com.github.laxika.magicalvibes.model.CardSubtype.SQUIRREL),
                    java.util.Set.of(), java.util.Set.of());
            var ctx = new TriggerContext.DamageToCreature(creature, 3, player1Id);

            when(gameQueryService.findPermanentController(gd, creature.getId())).thenReturn(player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2Id);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("does not queue a trigger when no damage was dealt")
        void skipsZeroDamage() {
            Permanent aura = createPermanent("Druid's Call");
            Permanent creature = createPermanent("Hill Giant");
            CreateTokenEffect effect = new CreateTokenEffect(new EventValue(), "Squirrel", 1, 1,
                    com.github.laxika.magicalvibes.model.CardColor.GREEN,
                    java.util.List.of(com.github.laxika.magicalvibes.model.CardSubtype.SQUIRREL),
                    java.util.Set.of(), java.util.Set.of());
            var ctx = new TriggerContext.DamageToCreature(creature, 0, player1Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — ReturnDamageSourcePermanentToHandEffect =====

    @Nested
    @DisplayName("ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE — ExileDamagedCreatureEffect")
    class ExileDamagedCreature {

        @Test
        @DisplayName("queues a non-targeting exile trigger for the damaged creature")
        void queuesExileTrigger() {
            Permanent source = createPermanent("Pit Spawn");
            Permanent damaged = createPermanent("Force of Nature");
            var effect = new ExileDamagedCreatureEffect();
            var ctx = new TriggerContext.CreatureDealsDamageToCreature(source, damaged.getId(), 2, false);

            boolean result = registry.dispatch(
                    match(source, player1Id, effect),
                    EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(damaged.getId());
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
            assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                    .isInstanceOf(com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect.class);
        }

        @Test
        @DisplayName("does not fire for another creature's damage")
        void doesNotFireForAnotherSource() {
            Permanent watcher = createPermanent("Pit Spawn");
            Permanent source = createPermanent("Grizzly Bears");
            Permanent damaged = createPermanent("Force of Nature");
            var effect = new ExileDamagedCreatureEffect();
            var ctx = new TriggerContext.CreatureDealsDamageToCreature(source, damaged.getId(), 2, false);

            boolean result = registry.dispatch(
                    match(watcher, player1Id, effect),
                    EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — ReturnDamageSourcePermanentToHandEffect")
    class BounceOnDamage {

        @Test
        @DisplayName("bounces the damage source to its owner's hand and returns true")
        void bouncesSourceAndReturnsTrue() {
            Permanent triggerPerm = createPermanent("Dissipation Field");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(permanentRemovalService.removePermanentToHand(gd, sourcePerm)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isTrue();
            verify(permanentRemovalService).removePermanentToHand(gd, sourcePerm);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("returns false when source permanent is no longer on the battlefield")
        void returnsFalseWhenSourceGone() {
            Permanent triggerPerm = createPermanent("Dissipation Field");
            UUID missingSourceId = UUID.randomUUID();
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToController(player1Id, missingSourceId, true);

            when(gameQueryService.findPermanentById(gd, missingSourceId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
        }

        @Test
        @DisplayName("does not broadcast when removePermanentToHand returns false")
        void noBroadcastWhenRemoveFails() {
            Permanent triggerPerm = createPermanent("Dissipation Field");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(permanentRemovalService.removePermanentToHand(gd, sourcePerm)).thenReturn(false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isTrue();
            verify(permanentRemovalService, never()).removeOrphanedAuras(any());
            verify(gameLogService, never()).append(any(), any(GameLogEntry.class));
        }
    }

    @Nested
    @DisplayName("ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — ExileDamageSourcePermanentUntilSourceLeavesEffect")
    class ExileDamageSourceOnDamage {

        @Test
        @DisplayName("queues a non-targeting exile trigger aimed at the damage source")
        void queuesExileTrigger() {
            Permanent triggerPerm = createPermanent("Hixus, Prison Warden");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new com.github.laxika.magicalvibes.model.effect
                    .ExileDamageSourcePermanentUntilSourceLeavesEffect(null, true);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(sourcePerm.getId());
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(triggerPerm.getId());
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("does not fire on noncombat damage when combatOnly is set")
        void skipsNoncombatDamage() {
            Permanent triggerPerm = createPermanent("Hixus, Prison Warden");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new com.github.laxika.magicalvibes.model.effect
                    .ExileDamageSourcePermanentUntilSourceLeavesEffect(null, true);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("does not fire when the intervening-if is not met")
        void skipsWhenInterveningIfUnmet() {
            Permanent triggerPerm = createPermanent("Hixus, Prison Warden");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var condition = new com.github.laxika.magicalvibes.model.condition.SourceEnteredThisTurn();
            var effect = new com.github.laxika.magicalvibes.model.effect
                    .ExileDamageSourcePermanentUntilSourceLeavesEffect(null, true, condition);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(conditionEvaluationService.isMet(eq(gd), eq(condition), any())).thenReturn(false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — DamageSourceControllerGainsControlOfThisPermanentEffect =====

    @Nested
    @DisplayName("ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — DamageSourceControllerGainsControlOfThisPermanentEffect")
    class ControlTheftOnDamage {

        @Test
        @DisplayName("steals the trigger permanent when combat damage from opponent creature")
        void stealsOnCombatDamage() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(true, true);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(gameQueryService.isCreature(gd, sourcePerm)).thenReturn(true);
            when(gameQueryService.findPermanentController(gd, sourcePerm.getId())).thenReturn(player2Id);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isTrue();
            verify(creatureControlService).applyControlEffect(eq(gd), eq(player2Id), eq(triggerPerm),
                    any(com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect.class),
                    eq(com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT),
                    any(), any());
        }

        @Test
        @DisplayName("returns false when combatOnly=true but damage is noncombat")
        void returnsFalseForNoncombatWhenCombatOnly() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(true, false);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
            verify(creatureControlService, never()).applyControlEffect(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("returns false when source permanent is gone")
        void returnsFalseWhenSourceGone() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            UUID missingSourceId = UUID.randomUUID();
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(false, false);
            var ctx = new TriggerContext.DamageToController(player1Id, missingSourceId, true);

            when(gameQueryService.findPermanentById(gd, missingSourceId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when creatureOnly=true but source is not a creature")
        void returnsFalseWhenSourceNotCreature() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Some Artifact");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(false, true);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(gameQueryService.isCreature(gd, sourcePerm)).thenReturn(false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when source controller is the same as damaged player")
        void returnsFalseWhenSourceControllerIsDamagedPlayer() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(false, false);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(gameQueryService.findPermanentController(gd, sourcePerm.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when source controller is null")
        void returnsFalseWhenSourceControllerNull() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(false, false);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(gameQueryService.findPermanentController(gd, sourcePerm.getId())).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("ON_DEALT_DAMAGE binds the source controller for control gain")
    void bindsSourceControllerForControlGain() {
        Permanent damagedCreature = createPermanent("Crag Saurian");
        var effect = new DamageSourceControllerGainsControlOfDamagedPermanentEffect();
        var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

        when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

        boolean result = registry.dispatch(
                match(damagedCreature, player1Id, effect),
                EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        var boundEffect = gd.stack.getFirst().getEffectsToResolve().getFirst();
        assertThat(boundEffect).isInstanceOf(DamageSourceControllerGainsControlOfDamagedPermanentEffect.class);
        assertThat(((DamageSourceControllerGainsControlOfDamagedPermanentEffect) boundEffect)
                .damageSourceControllerId()).isEqualTo(player2Id);
    }

    // ===== ON_DEALT_DAMAGE — DamageSourceControllerSacrificesPermanentsEffect =====

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — DamageSourceControllerSacrificesPermanentsEffect")
    class DamageSourceSacrifice {

        @Test
        @DisplayName("adds triggered ability to stack with damage count and source controller")
        void addsTriggeredAbilityWithDamageCount() {
            Permanent damagedCreature = createPermanent("Phyrexian Obliterator");
            var effect = new DamageSourceControllerSacrificesPermanentsEffect(0, null);
            UUID damageSourceControllerId = player2Id;
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 3, damageSourceControllerId);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getFirst();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getEffectsToResolve()).hasSize(1);
            assertThat(stackEntry.getEffectsToResolve().getFirst())
                    .isInstanceOf(DamageSourceControllerSacrificesPermanentsEffect.class);
            var resolvedEffect = (DamageSourceControllerSacrificesPermanentsEffect) stackEntry.getEffectsToResolve().getFirst();
            assertThat(resolvedEffect.count()).isEqualTo(3);
            assertThat(resolvedEffect.sacrificingPlayerId()).isEqualTo(damageSourceControllerId);
        }

        @Test
        @DisplayName("uses original effect when damageSourceControllerId is null")
        void usesOriginalEffectWhenControllerNull() {
            Permanent damagedCreature = createPermanent("Phyrexian Obliterator");
            var effect = new DamageSourceControllerSacrificesPermanentsEffect(5, player2Id);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 0, null);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var resolvedEffect = (DamageSourceControllerSacrificesPermanentsEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(resolvedEffect.count()).isEqualTo(5);
            assertThat(resolvedEffect.sacrificingPlayerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_DEALT_DAMAGE — DamageSourceControllerGetsPoisonCounterEffect =====

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — DamageSourceControllerGetsPoisonCounterEffect")
    class DamageSourcePoisonCounter {

        @Test
        @DisplayName("adds triggered ability with damage source controller")
        void addsTriggeredAbilityWithSourceController() {
            Permanent damagedCreature = createPermanent("Poisonous Creature");
            var effect = new DamageSourceControllerGetsPoisonCounterEffect(null);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var resolvedEffect = (DamageSourceControllerGetsPoisonCounterEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(resolvedEffect.damageSourceControllerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("uses original effect when damageSourceControllerId is null")
        void usesOriginalEffectWhenControllerNull() {
            Permanent damagedCreature = createPermanent("Poisonous Creature");
            var effect = new DamageSourceControllerGetsPoisonCounterEffect(player1Id);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, null);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var resolvedEffect = (DamageSourceControllerGetsPoisonCounterEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(resolvedEffect.damageSourceControllerId()).isEqualTo(player1Id);
        }
    }

    // ===== ON_CONTROLLER_DEALT_DAMAGE — PutCountersOnSelfEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DEALT_DAMAGE — PutCountersOnSelfEffect")
    class ControllerDealtDamagePutCounters {

        @Test
        @DisplayName("enqueues a triggered ability that snapshots the damage amount as eventValue")
        void enqueuesTriggerWithDamageAmount() {
            Permanent aura = createPermanent("Living Artifact");
            var effect = new PutCountersOnSelfEffect(CounterType.VITALITY, new EventValue());
            var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getFirst();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(stackEntry.getEventValue()).isEqualTo(3);
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("preserves an optional trigger and snapshots its damage amount")
        void enqueuesMayTriggerWithDamageAmount() {
            Permanent hound = createPermanent("Blood Hound");
            MayEffect effect = new MayEffect(
                    new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                    "Put that many +1/+1 counters on Blood Hound?");
            var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3);

            boolean result = registry.dispatch(
                    match(hound, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT source exclusion")
    class AllySourceDealtDamageToOpponentSourceExclusion {

        @Test
        @DisplayName("does not trigger when the excluded watcher is the damage source")
        void excludesTheDamageSource() {
            Permanent talon = createPermanent("Talon of Pain");
            var effect = new PutCountersOnSelfEffect(CounterType.CHARGE, true);
            var ctx = new TriggerContext.DamageToControllerAmount(player2Id, 2, talon.getId());

            boolean result = registry.dispatch(
                    match(talon, player1Id, effect),
                    EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("triggers when another source deals the damage")
        void acceptsAnotherDamageSource() {
            Permanent talon = createPermanent("Talon of Pain");
            var effect = new PutCountersOnSelfEffect(CounterType.CHARGE, true);
            var ctx = new TriggerContext.DamageToControllerAmount(player2Id, 2, UUID.randomUUID());

            boolean result = registry.dispatch(
                    match(talon, player1Id, effect),
                    EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT conditional may")
    class ControllerDealtDamageByOpponentConditionalMay {

        @Test
        @DisplayName("queues the conditional may trigger when its intervening-if is met")
        void queuesWhenConditionIsMet() {
            Permanent mask = createPermanent("Farsight Mask");
            var condition = new SourceUntapped();
            var effect = new ConditionalEffect(condition, new MayEffect(new DrawCardEffect(), "Draw a card?"));
            var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3);

            when(conditionEvaluationService.isInterveningIfMet(eq(gd), eq(effect), eq(mask), eq(player1Id)))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(mask, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("does not queue the trigger when its intervening-if is not met")
        void skipsWhenConditionIsNotMet() {
            Permanent mask = createPermanent("Farsight Mask");
            var condition = new SourceUntapped();
            var effect = new ConditionalEffect(condition, new MayEffect(new DrawCardEffect(), "Draw a card?"));
            var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3);

            when(conditionEvaluationService.isInterveningIfMet(eq(gd), eq(effect), eq(mask), eq(player1Id)))
                    .thenReturn(false);

            boolean result = registry.dispatch(
                    match(mask, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("does not queue a conditional self-damage trigger when its intervening-if is false")
    void skipsConditionalSelfDamageTriggerWhenInterveningIfIsFalse() {
        Permanent source = createPermanent("Kiyomaro, First to Stand");
        var condition = new SourceUntapped();
        var effect = new ConditionalEffect(condition, new DrawCardEffect());
        var ctx = new TriggerContext.SourceDealsDamage(source.getCard(), player1Id, 2,
                Map.of(player2Id, 2));

        when(conditionEvaluationService.isMet(eq(gd), eq(condition), any(ConditionContext.class)))
                .thenReturn(false);

        boolean result = registry.dispatch(
                match(source, player1Id, effect), EffectSlot.ON_SELF_DEALS_DAMAGE, effect, ctx);

        assertThat(result).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("evaluates a self-damage condition from the source card after the source dies")
    void evaluatesSelfDamageConditionAfterSourceDies() {
        Permanent source = createPermanent("Kiyomaro, First to Stand");
        var condition = new SourceUntapped();
        var effect = new ConditionalEffect(condition, new DrawCardEffect());
        var ctx = new TriggerContext.SourceDealsDamage(source.getCard(), player1Id, 2,
                Map.of(player2Id, 2));

        when(conditionEvaluationService.isMet(eq(gd), eq(condition), any(ConditionContext.class)))
                .thenReturn(true);

        boolean result = registry.dispatch(
                match(null, player1Id, effect), EffectSlot.ON_SELF_DEALS_DAMAGE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isNull();
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);

        ArgumentCaptor<ConditionContext> contextCaptor = ArgumentCaptor.forClass(ConditionContext.class);
        verify(conditionEvaluationService).isMet(eq(gd), eq(condition), contextCaptor.capture());
        assertThat(contextCaptor.getValue().sourcePermanent()).isNull();
        assertThat(contextCaptor.getValue().sourceCard()).isSameAs(source.getCard());
    }

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — ConditionalEffect")
    class DealtDamageConditional {

        @Test
        @DisplayName("queues the trigger when the damage event meets the threshold")
        void queuesWhenDamageMeetsThreshold() {
            Permanent damagedCreature = createPermanent("Innocent Bystander");
            var condition = new EventValueAtLeast(3);
            var effect = new ConditionalEffect(condition, new DrawCardEffect());
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 3, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);
            when(conditionEvaluationService.isMet(eq(gd), eq(condition), any(ConditionContext.class), eq(3)))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect), EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("does not queue the trigger when the damage event is below the threshold")
        void skipsWhenDamageIsBelowThreshold() {
            Permanent damagedCreature = createPermanent("Innocent Bystander");
            var condition = new EventValueAtLeast(3);
            var effect = new ConditionalEffect(condition, new DrawCardEffect());
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);
            when(conditionEvaluationService.isMet(eq(gd), eq(condition), any(ConditionContext.class), eq(2)))
                    .thenReturn(false);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect), EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_DEALT_DAMAGE — default handler =====

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — default handler")
    class DealtDamageDefault {

        @Test
        @DisplayName("queues a player-only target choice for a target-player damage trigger")
        void queuesPlayerOnlyTargetChoice() {
            Card damagedCard = createCard("Truefire Captain");
            damagedCard.target(TargetFilters.creature());
            var effect = new DealDamageToTargetPlayerOrPlaneswalkerEffect(new XValue());
            damagedCard.target(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"))
                    .addEffect(EffectSlot.ON_DEALT_DAMAGE, effect);
            Permanent damagedCreature = new Permanent(damagedCard);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 3, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.playerTargetOnly()).isTrue();
            assertThat(pending.spellManaSpentX()).isEqualTo(3);
            assertThat(pending.targetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        }

        @Test
        @DisplayName("does not add to stack when controller is null")
        void doesNotAddWhenControllerNull() {
            Permanent damagedCreature = createPermanent("Grizzly Bears");
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(null);

            registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("adds triggered ability for unrecognized effect via default handler")
        void addsTriggeredAbilityForUnrecognizedEffect() {
            Permanent damagedCreature = createPermanent("Custom Creature");
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getFirst();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("queues a target choice for a mandatory targeted effect")
        void queuesTargetChoiceForTargetedEffect() {
            Card sourceCard = createCard("Trapjaw Tyrant");
            var effect = new ExileTargetPermanentUntilSourceLeavesEffect();
            sourceCard.target(new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature"))
                    .addEffect(EffectSlot.ON_DEALT_DAMAGE, effect);
            Permanent damagedCreature = new Permanent(sourceCard);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.controllerId()).isEqualTo(player1Id);
            assertThat(pending.sourcePermanentId()).isEqualTo(damagedCreature.getId());
            assertThat(pending.spellManaSpentX()).isEqualTo(2);
            assertThat(pending.effects()).containsExactly(effect);
            assertThat(pending.targetFilter()).isEqualTo(sourceCard.getTargetFilter());
        }
    }
}
