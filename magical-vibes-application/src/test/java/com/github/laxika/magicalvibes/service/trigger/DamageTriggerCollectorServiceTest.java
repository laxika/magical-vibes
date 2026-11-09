package com.github.laxika.magicalvibes.service.trigger;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceCreatureOrSpellControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private TapUntapSupport tapUntapSupport;

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

    @Test
    void playerCombatDamageEquipmentTriggerUsesEquipmentController() {
        Permanent equipment = createPermanent("Equipment");
        Permanent attacker = createPermanent("Attacker");
        DrawCardEffect effect = new DrawCardEffect(1);

        registry.dispatch(match(equipment, player1Id, effect),
                CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER, effect,
                new TriggerContext.SourceDealsCombatDamage(attacker.getCard(), player2Id,
                        attacker.getId(), 3, 3));

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(equipment.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        });
    }

    @Test
    @DisplayName("queues a global permanent damage trigger whose filter the collector judged")
    void queuesGlobalPermanentDamageTriggerWhoseFilterTheCollectorJudged() {
        Permanent watcher = createPermanent("Wrathful Raptors");
        Permanent damaged = createPermanent("Dinosaur");
        var wrapped = new DealDamageToAnyTargetEffect(new XValue());
        var ctx = new TriggerContext.AnyPermanentDealtDamage(damaged, player1Id, 2);

        boolean result = registry.dispatch(
                match(watcher, player1Id, wrapped), DamageWatch.ON_ANY_PERMANENT_DEALT_DAMAGE, wrapped, ctx);

        assertThat(result).isTrue();
        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class))
                .isNotNull()
                .satisfies(pending -> {
                    assertThat(pending.effects()).containsExactly(wrapped);
                    // The ability is the watcher's (CR 113.7); the damaged permanent is the event's.
                    assertThat(pending.sourcePermanentId()).isEqualTo(watcher.getId());
                    assertThat(pending.triggeringPermanentId()).isEqualTo(damaged.getId());
                    assertThat(pending.departedPermanent().getId()).isEqualTo(damaged.getId());
                    assertThat(pending.eventValue()).isEqualTo(2);
                });
    }

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
    @DisplayName("queues a combat-only planeswalker trigger for combat damage")
    void queuesCombatOnlyPlaneswalkerTriggerForCombatDamage() {
        Permanent watcher = createPermanent("Zagras, Thief of Heartbeats");
        Permanent source = createPermanent("Grizzly Bears");
        Permanent planeswalker = createPermanent("Test Planeswalker");
        var effect = new DestroyTargetPermanentEffect();
        var ctx = new TriggerContext.CreatureDealsDamageToPlaneswalker(
                source, planeswalker.getId(), 2, true);

        when(gameQueryService.isCreature(gd, source)).thenReturn(true);

        boolean result = registry.dispatch(
                match(watcher, player1Id, effect),
                DamageWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLANESWALKER, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(planeswalker.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
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
                DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);

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
                DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("queues one trigger when a controlled Hero deals damage to a player")
    void queuesBatchedHeroDamageTrigger() {
        Permanent watcher = createPermanent("The Thing, Ben Grimm");
        Permanent hero = createPermanent("Agent Maria Hill");
        var wrapped = new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2);
        var ctx = new TriggerContext.AllyCreaturesDealDamageToPlayer(
                player2Id, player1Id, List.of(hero));

        boolean result = registry.dispatch(
                match(watcher, player1Id, wrapped),
                DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER, wrapped, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2Id);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(wrapped);
    }

    @Test
    @DisplayName("routes self combat-damage graveyard targets through the graveyard choice")
    void queuesSelfCombatDamageGraveyardTarget() {
        Permanent source = createPermanent("Archpriest of Shadows");
        var effect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                source.getCard(), player1Id, source.getId(), 4);

        boolean result = registry.dispatch(
                match(source, player1Id, effect),
                CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
                effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)).isTrue();
        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class).minCount())
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Nested
    @DisplayName("ON_ANY_CREATURE_DEALT_DAMAGE — permanent conditional")
    class AnyCreatureDealtDamageConditional {

        @Test
        @DisplayName("queues the effect at the damaged creature once the collector judged the filter")
        void queuesEffectAtTheDamagedCreature() {
            Permanent watcher = createPermanent("Rite of Passage");
            Permanent damaged = createPermanent("Hill Giant");
            var wrapped = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE);
            var ctx = new TriggerContext.AnyCreatureDealtDamage(damaged, player1Id, 2);

            boolean result = registry.dispatch(
                    match(watcher, player1Id, wrapped), DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE, wrapped, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(damaged.getId());
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(wrapped);
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        }

        @Test
        @DisplayName("carries \"do this only once each turn\" into the target choice, marked as the action is taken")
        void carriesDoThisOnlyOnceEachTurnIntoTheTargetChoice() {
            Permanent watcher = createPermanent("The Sensational She-Hulk");
            Permanent damaged = createPermanent("Hill Giant");
            MayEffect may = new MayEffect(
                    new com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect(new EventValue()),
                    "Reflect the damage?");
            var ctx = new TriggerContext.AnyCreatureDealtDamage(damaged, player1Id, 2);

            assertThat(registry.dispatch(new TriggerMatchContext(gd, watcher, player1Id, may, watcher.getCard(), true),
                    DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE, may, ctx)).isTrue();

            assertThat(gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class))
                    .isNotNull()
                    .satisfies(pending -> {
                        assertThat(pending.effects()).containsExactly(may);
                        assertThat(pending.markSourceOncePerTurnOnAcceptance()).isTrue();
                    });
            assertThat(gd.oncePerTurnTriggersFiredThisTurn).doesNotContain(watcher.getId());
        }
    }

    @Nested
    @DisplayName("ON_ENCHANTED_CREATURE_DEALT_DAMAGE — the default records the event whatever the effect is")
    class CreateTokensOnEnchantedCreatureDamage {

        @Test
        @DisplayName("queues a self-sacrifice trigger for damage to an enchanted creature")
        void queuesSelfSacrificeForEnchantedCreatureDamage() {
            Permanent aura = createPermanent("Sleep Magic");
            Permanent creature = createPermanent("Hill Giant");
            var effect = new SacrificeSelfEffect();
            var ctx = new TriggerContext.DamageToCreature(creature, 2, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1Id);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("queues a generic effect and records damage")
        void queuesGenericEffect() {
            Permanent aura = createPermanent("Soul Link");
            Permanent creature = createPermanent("Hill Giant");
            GainLifeEffect effect = new GainLifeEffect(new EventValue());
            var ctx = new TriggerContext.DamageToCreature(creature, 3, player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1Id);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("the Aura's controller controls the ability; the entry records the creature and its controller")
        void recordsTheEnchantedCreatureAndItsControllerForTheAurasAbility() {
            Permanent aura = createPermanent("Druid's Call");
            Permanent creature = createPermanent("Hill Giant");
            var effect = new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect(new EventValue(), "Squirrel",
                    1, 1, com.github.laxika.magicalvibes.model.CardColor.GREEN,
                    java.util.List.of(com.github.laxika.magicalvibes.model.CardSubtype.SQUIRREL),
                    java.util.Set.of(), java.util.Set.of()), false, true);
            var ctx = new TriggerContext.DamageToCreature(creature, 3, player1Id);

            when(gameQueryService.findPermanentController(gd, creature.getId())).thenReturn(player2Id);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(creature.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            assertThat(entry.getTriggeringPermanentSnapshot().getId()).isEqualTo(creature.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
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
                    DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE — no class-keyed handler")
    class AllyDealtDamageToCreatureReflection {

        @Test
        @DisplayName("the abilities the cards write with trigger filters have no class-keyed handler on the watch")
        void theCardsEffectsHaveNoClassKeyedHandler() {
            assertThat(registry.handles(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                    new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST), "Put a quest counter?")))
                    .isFalse();
            assertThat(registry.handles(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                    new PutCountersOnSourceEffect(1, 1, 1))).isFalse();
            assertThat(registry.handles(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                    new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)
                            .dealtByEventDamageSource())).isFalse();
            assertThat(registry.handles(DamageWatch.ON_SELF_DEALS_DAMAGE_TO_CREATURE,
                    com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect.ofTriggeringPermanent()))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — that source and that source's controller")
    class DealtDamageSource {

        @Test
        @DisplayName("The default queues the effect unchanged, with the damage source as the triggering permanent")
        void defaultCarriesTheDamageSourceAsTheTriggeringPermanent() {
            Permanent damagedCreature = createPermanent("Phyrexian Obliterator");
            Permanent source = createPermanent("Grizzly Bears");
            var effect = new SacrificePermanentsEffect(new EventValue(), new PermanentTruePredicate(),
                    SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 3, player2Id, source.getCard(),
                    source.getId());

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    DamageWatch.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getEventValue()).isEqualTo(3);
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(source.getId());
                assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            });
        }

        @Test
        @DisplayName("A spell source has no triggering permanent, only its controller")
        void spellSourceRecordsOnlyItsController() {
            Permanent damagedCreature = createPermanent("Tephraderm");
            var effect = new DealDamageToDamageSourceCreatureOrSpellControllerEffect();
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id, createCard("Shock"), null);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            registry.dispatch(match(damagedCreature, player1Id, effect), DamageWatch.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getTriggeringPermanentId()).isNull();
                assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
                assertThat(entry.getEventValue()).isEqualTo(2);
            });
        }

        @Test
        @DisplayName("Archfiend of Spite triggers for a source an opponent controls")
        void archfiendTriggersForAnOpponentsSource() {
            Permanent archfiend = createPermanent("Archfiend of Spite");
            Permanent source = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect();
            var ctx = new TriggerContext.DamageToCreature(archfiend, 2, player2Id, source.getCard(), source.getId());

            when(gameQueryService.findPermanentController(gd, archfiend.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(match(archfiend, player1Id, effect), DamageWatch.ON_DEALT_DAMAGE,
                    effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(source.getId());
                assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            });
        }

        @Test
        @DisplayName("Archfiend of Spite doesn't trigger for a source its controller controls")
        void archfiendDoesNotTriggerForItsControllersSource() {
            Permanent archfiend = createPermanent("Archfiend of Spite");
            Permanent source = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect();
            var ctx = new TriggerContext.DamageToCreature(archfiend, 2, player1Id, source.getCard(), source.getId());

            boolean result = registry.dispatch(match(archfiend, player1Id, effect), DamageWatch.ON_DEALT_DAMAGE,
                    effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== ON_CONTROLLER_DEALT_DAMAGE — PutCountersOnSelfEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DEALT_DAMAGE — PutCountersOnSelfEffect")
    class ControllerDealtDamagePutCounters {

        @Test
        void genericDamageTriggerPreservesSacrificeEffectAndDamageAmount() {
            Permanent lich = createPermanent("Lich");
            var effect = new SacrificePermanentsOrLoseGameEffect(new EventValue(),
                    new PermanentNotPredicate(new PermanentIsTokenPredicate()));
            var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 4);

            boolean result = registry.dispatch(match(lich, player1Id, effect),
                    DamageWatch.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var entry = gd.stack.getFirst();
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(lich.getId());
            assertThat(entry.getEventValue()).isEqualTo(4);
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }

        @Test
        @DisplayName("enqueues a triggered ability that snapshots the damage amount as eventValue")
        void enqueuesTriggerWithDamageAmount() {
            Permanent aura = createPermanent("Living Artifact");
            var effect = new PutCountersOnSelfEffect(CounterType.VITALITY, new EventValue());
            var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3);

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    DamageWatch.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);

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
                    DamageWatch.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DEALT_DAMAGE — SacrificePermanentsOrLoseGameEffect")
    class ControllerDealtDamageSacrificeOrLose {

        @Test
        @DisplayName("enqueues a non-targeting trigger with the damage amount")
        void enqueuesTriggerWithDamageAmount() {
            Permanent lich = createPermanent("Lich");
            var effect = new SacrificePermanentsOrLoseGameEffect(
                    new EventValue(), new PermanentNotPredicate(new PermanentIsTokenPredicate()));
            var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3);

            boolean result = registry.dispatch(
                    match(lich, player1Id, effect),
                    DamageWatch.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
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
                    DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect, ctx);

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
                    DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
        }
    }

    @Nested
    @DisplayName("ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT creature damage")
    class AllySourceDealtNoncombatDamageToOpponentCreatureDamage {

        @Test
        @DisplayName("queues the conditional creature damage's target choice with the damaged player and the damage")
        void queuesConditionalCreatureDamageTargetChoice() {
            Permanent watcher = createPermanent("Fear of Burning Alive");
            var effect = new ConditionalEffect(new Delirium(),
                    new DealDamageToTargetCreatureEffect(new EventValue(), false,
                            new PermanentControlledByTriggeringPlayerPredicate()));
            var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id, player1Id, 3);

            boolean result = registry.dispatch(
                    match(watcher, player1Id, effect),
                    DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).isEmpty();
            var choice = gd.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
            assertThat(choice.effects()).containsExactly(effect);
            assertThat(choice.triggeringPlayerId()).isEqualTo(player2Id);
            assertThat(choice.eventValue()).isEqualTo(3);
            assertThat(choice.choosingPlayerId()).isEqualTo(player1Id);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT conditional may")
    class ControllerDealtDamageByOpponentConditionalMay {

        @Test
        @DisplayName("queues the conditional may trigger, its intervening-if wrapped to be checked as it resolves")
        void queuesWhenConditionIsMet() {
            Permanent mask = createPermanent("Farsight Mask");
            var condition = new SourceUntapped();
            var effect = new ConditionalEffect(condition, new MayEffect(new DrawCardEffect(), "Draw a card?"));
            var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3);

            boolean result = registry.dispatch(
                    match(mask, player1Id, effect),
                    DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("queues an intervening-if noncombat-damage token trigger, kept to be checked again as it resolves")
    void queuesInterveningIfNoncombatDamageTokenTrigger() {
        Permanent perm = createPermanent("Molten Lavamancer");
        var effect = new ConditionalEffect(new ControllerTurn(), elementalToken());
        var ctx = new TriggerContext.NoncombatDamageToOpponent(player2Id, player1Id, 1);

        assertThat(registry.dispatch(match(perm, player1Id, effect),
                DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT, effect, ctx)).isTrue();

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).containsExactly(effect));
    }

    private static CreateTokenEffect elementalToken() {
        return new CreateTokenEffect(1, "Elemental", 1, 1, com.github.laxika.magicalvibes.model.CardColor.RED,
                List.of(CardSubtype.ELEMENTAL), java.util.Set.of(), java.util.Set.of());
    }

    @Test
    @DisplayName("queues an unrecognized opponent-damage effect through the default handler")
    void queuesUnrecognizedControllerDealtDamageByOpponentEffect() {
        Permanent watcher = createPermanent("Awaken the Sky Tyrant");
        var effect = new SacrificeSelfThenEffect(new DrawCardEffect());
        var ctx = new TriggerContext.DamageToControllerAmount(player1Id, 3, player2Id);

        boolean result = registry.dispatch(
                match(watcher, player1Id, effect),
                DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("queues a conditional self-damage trigger from the source card after the source dies")
    void evaluatesSelfDamageConditionAfterSourceDies() {
        Permanent source = createPermanent("Kiyomaro, First to Stand");
        var condition = new SourceUntapped();
        var effect = new ConditionalEffect(condition, new DrawCardEffect());
        var ctx = new TriggerContext.SourceDealsDamage(source.getCard(), player1Id, 2,
                Map.of(player2Id, 2));

        boolean result = registry.dispatch(
                match(null, player1Id, effect), DamageWatch.ON_SELF_DEALS_DAMAGE, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isNull();
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
    }

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — ConditionalEffect")
    class DealtDamageConditional {

        @Test
        @DisplayName("queues the conditional trigger with the damage dealt, its condition checked as it resolves")
        void queuesWhenDamageMeetsThreshold() {
            Permanent damagedCreature = createPermanent("Innocent Bystander");
            var condition = new EventValueAtLeast(3);
            var effect = new ConditionalEffect(condition, new DrawCardEffect());
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 3, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);
            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect), DamageWatch.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
        }

    }

    @Nested
    @DisplayName("ON_OPPONENT_DEALT_DAMAGE — ConditionalEffect")
    class OpponentDealtDamageConditional {

        @Test
        @DisplayName("queues the conditional trigger with the damaged player and the damage dealt")
        void queuesWhenDamageMeetsThreshold() {
            gd.playerIds.add(player1Id);
            gd.playerIds.add(player2Id);
            Permanent watcher = createPermanent("Pain Magnification");
            var condition = new EventValueAtLeast(3);
            var effect = new ConditionalEffect(condition,
                    new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
            var ctx = new TriggerContext.DamageToControllerAmount(player2Id, 3, UUID.randomUUID());

            boolean result = registry.dispatch(
                    match(watcher, player1Id, effect), DamageWatch.ON_OPPONENT_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2Id);
            assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(3);
            assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
            assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
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
                    .addTrigger(Triggers.dealtDamage(), effect);
            Permanent damagedCreature = new Permanent(damagedCard);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 3, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    DamageWatch.ON_DEALT_DAMAGE, effect, ctx);

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
            var effect = com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect.triggering();
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(null);

            registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    DamageWatch.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("adds triggered ability for unrecognized effect via default handler")
        void addsTriggeredAbilityForUnrecognizedEffect() {
            Permanent damagedCreature = createPermanent("Custom Creature");
            var effect = com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect.triggering();
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    DamageWatch.ON_DEALT_DAMAGE, effect, ctx);

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
                    .addTrigger(Triggers.dealtDamage(), effect);
            Permanent damagedCreature = new Permanent(sourceCard);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    DamageWatch.ON_DEALT_DAMAGE, effect, ctx);

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

    @Test
    @DisplayName("queues a filtered permanent-damage trigger and preserves its target filter")
    void queuesFilteredAnyPermanentDamageTrigger() {
        Card watcherCard = createCard("Wrathful Red Dragon");
        var targetFilter = new AnyTargetPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must not be a Dragon");
        var damage = new DealDamageToAnyTargetEffect(new XValue());
        watcherCard.target(targetFilter).addTrigger(Triggers.permanentYouControlDealtDamage()
                .withFilter(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)), damage);
        Permanent watcher = new Permanent(watcherCard);
        Permanent damaged = createPermanent("Dragon");
        var ctx = new TriggerContext.AnyPermanentDealtDamage(damaged, player1Id, 3);

        boolean result = registry.dispatch(
                match(watcher, player1Id, damage), DamageWatch.ON_ANY_PERMANENT_DEALT_DAMAGE, damage, ctx);

        assertThat(result).isTrue();
        var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
        assertThat(pending).isNotNull();
        assertThat(pending.spellManaSpentX()).isEqualTo(3);
        assertThat(pending.targetFilter()).isEqualTo(targetFilter);
        assertThat(pending.effects()).containsExactly(damage);
    }

    /**
     * The damage collectors put the card's effect, or a trigger-condition record's {@code triggeredEffect()}, on the
     * stack unchanged: "that much" is the event value and "that source" / "that creature" the triggering permanent.
     */
    @Nested
    @DisplayName("Damage effects read the stack entry")
    class DamageEffectsReadTheEntry {

        @Test
        @DisplayName("Counters put: the authored damage goes to the target choice with the counters as its event value")
        void countersPlacedCarriesTheCountAsTheEventValue() {
            Permanent watcher = createPermanent("All Will Be One");
            var damage = new DealDamageToAnyTargetEffect(new EventValue());
            var ctx = new TriggerContext.CountersPlaced(player1Id, 4);

            boolean result = registry.dispatch(match(watcher, player1Id, damage),
                    CounterWatch.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER, damage, ctx);

            assertThat(result).isTrue();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.effects()).containsExactly(damage);
            assertThat(pending.spellManaSpentX()).isEqualTo(4);
        }

        @Test
        @DisplayName("Opponent source damage: the \"you may\" goes on the stack unchanged, the source its triggering permanent")
        void opponentSourceDamageMayRecordsTheSource() {
            Permanent watcher = createPermanent("Flameblade Angel");
            Permanent source = createPermanent("Shock source");
            var may = new MayEffect(new com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect(1,
                    com.github.laxika.magicalvibes.model.effect.DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER),
                    "Deal 1 damage?");
            var ctx = new TriggerContext.SourceDamageToYouOrYourPermanent(source.getCard(), player2Id,
                    source.getId(), player1Id);

            boolean result = registry.dispatch(match(watcher, player1Id, may),
                    DamageWatch.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT, may, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(may);
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(source.getId());
                assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            });
        }

        @Test
        @DisplayName("The source, damage-to-you, chosen-color creature and fight abilities' effects go on the stack through no record handler")
        void theSourceDamageAbilitiesHaveNoRecordHandler() {
            var gainLife = new GainLifeEffect(new EventValue());
            var destroy = new com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect(
                    PermanentReference.TRIGGERING);
            assertThat(registry.handles(DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE, gainLife)).isFalse();
            assertThat(registry.handles(DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE, gainLife)).isFalse();
            assertThat(registry.handles(DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, destroy)).isFalse();
            assertThat(registry.handles(DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
                    new com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentEffect(
                            new EventValue()))).isFalse();
        }

        @Test
        @DisplayName("The deals-damage-to-you, chosen-color source and only-target-creature abilities' effects go on the stack through no record handler")
        void theDamageToYouAbilitiesHaveNoRecordHandler() {
            assertThat(registry.handles(DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
                    com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect.triggering())).isFalse();
            assertThat(registry.handles(DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
                    new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.GOLD)))
                    .isFalse();
            assertThat(registry.handles(DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE_TO_YOU,
                    new com.github.laxika.magicalvibes.model.effect.MayPayManaEffect("{1}", new GainLifeEffect(1),
                            "Pay {1}?"))).isFalse();
            assertThat(registry.handles(DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE,
                    new com.github.laxika.magicalvibes.model.effect.MayPayManaEffect("{1}", new GainLifeEffect(1),
                            "Pay {1}?"))).isFalse();
            assertThat(registry.handles(DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_ITS_ONLY_TARGET_CREATURE,
                    new com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect(new EventValue(),
                            com.github.laxika.magicalvibes.model.effect.DamageRecipient.EACH_OPPONENT))).isFalse();
        }

        @Test
        @DisplayName("A creature you control fights: the card's delayed counters go on the stack unchanged, the fighter its triggering permanent")
        void fightGoesOnTheStackUnchangedWithTheFighter() {
            Permanent watcher = createPermanent("Foe-Razer Regent");
            Permanent fighter = createPermanent("Grizzly Bears");
            var delayed = new com.github.laxika.magicalvibes.model.effect.RegisterDelayedCountersOnTriggeringPermanentEffect(
                    new com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect(
                            CounterType.PLUS_ONE_PLUS_ONE, 2));
            var ctx = new TriggerContext.CreatureFights(fighter);

            boolean result = registry.dispatch(match(watcher, player1Id, delayed),
                    ActionWatch.ON_ALLY_CREATURE_FIGHTS, delayed, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(delayed);
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(fighter.getId());
            });
        }

        @Test
        @DisplayName("Enchanted creature dealt damage: \"destroy it\" goes on the stack unchanged, the creature its triggering permanent")
        void enchantedCreatureDestroyGoesOnTheStackUnchanged() {
            Permanent aura = createPermanent("Mortal Wound");
            Permanent creature = createPermanent("Grizzly Bears");
            var destroy = new com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect(
                    PermanentReference.TRIGGERING);
            when(gameQueryService.findPermanentController(gd, creature.getId())).thenReturn(player2Id);
            var ctx = new TriggerContext.DamageToCreature(creature, 2, player1Id);

            boolean result = registry.dispatch(match(aura, player1Id, destroy),
                    DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, destroy, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(destroy);
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(creature.getId());
            });
        }
    }
}
