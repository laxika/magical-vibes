package com.github.laxika.magicalvibes.service.trigger;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscardTriggerCollectorServiceTest {

    @Mock
    private GameLogService gameLogService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private DamagePreventionService damagePreventionService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @Mock
    private LifeSupport lifeSupport;

    @Mock
    private ConditionEvaluationService conditionEvaluationService;

    @InjectMocks
    private DiscardTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        lenient().when(gameQueryService.lifeAfterDamage(eq(gd), any(UUID.class), anyInt()))
                .thenAnswer(invocation -> gd.getLife(invocation.getArgument(1))
                        - (int) invocation.getArgument(2));
        lenient().when(gameQueryService.opponentLifeLossMultiplier(eq(gd), any(UUID.class))).thenReturn(1);
        lenient().when(damagePreventionService.applyChannelHarmPrevention(
                        eq(gd), any(UUID.class), org.mockito.ArgumentMatchers.nullable(UUID.class), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(3));

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    @Test
    @DisplayName("Queues a conditional discard trigger only when its condition is met")
    void queuesConditionalDiscardTriggerWhenConditionIsMet() {
        Permanent source = createPermanent("Spacecraft");
        var condition = new SourceCounterThreshold(1, CounterType.CHARGE);
        var wrapped = new DrawCardEffect(1);
        var effect = new ConditionalEffect(condition, wrapped);
        var ctx = new TriggerContext.Discard(player2Id, createCard("Discarded card"));
        when(conditionEvaluationService.isMet(eq(gd), eq(condition), any(ConditionContext.class)))
                .thenReturn(true);

        boolean result = registry.dispatch(
                match(source, player1Id, effect), EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getSourcePermanentId()).isEqualTo(source.getId());
        });
    }

    // ===== Helpers =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    // ===== ON_OPPONENT_DISCARDS — DealDamageToDiscardingPlayerEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — DealDamageToDiscardingPlayerEffect")
    class DiscardDamage {

        @Test
        @DisplayName("deals damage to the discarding player and returns true")
        void dealsDamageToDiscardingPlayer() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore - 2);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("does not deal damage when source damage is prevented globally")
        void noDamageWhenSourcePrevented() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.isDamageFromPermanentSourcePrevented(gd, megrim)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when source damage is prevented for player")
        void noDamageWhenSourcePreventedForPlayer() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.isSourceDamagePreventedForPlayer(gd, player2Id, megrim.getId()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when color damage prevention applies")
        void noDamageWhenColorPreventionApplies() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(player2Id), any()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when permanent is prevented from dealing damage")
        void noDamageWhenPermanentPrevented() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            gd.permanentsPreventedFromDealingDamage.add(megrim.getId());
            int lifeBefore = gd.getLife(player2Id);

            boolean result = registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("tracks player as dealt damage this turn")
        void tracksPlayerDealtDamageThisTurn() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.playersDealtDamageThisTurn).contains(player2Id);
        }

        @Test
        @DisplayName("gives poison counters when damage should be dealt as infect")
        void givesPoisonCountersWithInfect() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            when(gameQueryService.shouldDamageBeDealtAsInfect(gd, player2Id)).thenReturn(true);

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            verify(lifeSupport).applyPoisonCounters(gd, player2Id, 2, "Megrim", player1Id);
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not give poison counters when player can't get them")
        void noPoisonWhenPlayerCantGetThem() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            when(gameQueryService.shouldDamageBeDealtAsInfect(gd, player2Id)).thenReturn(true);
            // canPlayerGetPoisonCounters defaults to false

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.playerPoisonCounters).doesNotContainKey(player2Id);
        }

        @Test
        @DisplayName("does not track damage when prevention shield reduces damage to zero")
        void noDamageTrackingWhenShieldReducesToZero() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(0);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(0), any()))
                    .thenReturn(0);

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
            assertThat(gd.playersDealtDamageThisTurn).doesNotContain(player2Id);
        }

        @Test
        @DisplayName("does not change life when player life can't change")
        void noLifeChangeWhenPrevented() {
            Permanent megrim = createPermanent("Megrim");
            var effect = new DealDamageToDiscardingPlayerEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            // canPlayerLifeChange defaults to false — life can't change

            registry.dispatch(
                    match(megrim, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }
    }

    // ===== ON_OPPONENT_DISCARDS — LoseLifeEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — LoseLifeEffect")
    class DiscardLifeLoss {

        @Test
        @DisplayName("causes the discarding player to lose life and returns true")
        void causesLifeLossOnDiscard() {
            Permanent enchantment = createPermanent("Liliana's Caress");
            var effect = new LoseLifeEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore - 2);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("causes the controller to lose life on their own discard")
        void causesControllerLifeLossOnDiscard() {
            Permanent enchantment = createPermanent("Midnight Oil");
            var effect = new LoseLifeEffect(1);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player1Id);

            when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player1Id)).isEqualTo(lifeBefore - 1);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("does not change life when player life can't change")
        void noLifeLossWhenPrevented() {
            Permanent enchantment = createPermanent("Liliana's Caress");
            var effect = new LoseLifeEffect(2);
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            int lifeBefore = gd.getLife(player2Id);

            // canPlayerLifeChange defaults to false — life can't change

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }
    }

    // ===== ON_OPPONENT_DISCARDS — MayEffect =====

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — MayEffect")
    class DiscardMay {

        @Test
        @DisplayName("queues may ability and returns true")
        void queuesMayAbility() {
            Permanent enchantment = createPermanent("Waste Not");
            var inner = new LoseLifeEffect(1);
            var effect = new MayEffect(inner, "Do you want to trigger?");
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(enchantment, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — ExileDiscardedCardFromGraveyardEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — ExileDiscardedCardFromGraveyardEffect")
    class ControllerDiscardExile {

        @Test
        @DisplayName("queues the trigger with the discarded card and returns true")
        void queuesDiscardTrigger() {
            Permanent necro = createPermanent("Necropotence");
            var effect = new ExileDiscardedCardFromGraveyardEffect();
            Card discarded = createCard("Grizzly Bears");
            gd.playerGraveyards.computeIfAbsent(player1Id, k -> new ArrayList<>()).add(discarded);
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(necro, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(necro.getId());
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(discarded.getId());
            assertThat(gd.playerGraveyards.get(player1Id)).contains(discarded);
        }

        @Test
        @DisplayName("tracks the discarded card with the source when requested")
        void tracksExiledCardWithSource() {
            Permanent bag = createPermanent("Bag of Holding");
            var effect = new ExileDiscardedCardFromGraveyardEffect(true);
            Card discarded = createCard("Grizzly Bears");
            gd.playerGraveyards.computeIfAbsent(player1Id, k -> new ArrayList<>()).add(discarded);
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(bag, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(bag.getId());
            assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(discarded.getId());
        }

        @Test
        @DisplayName("no-op when the discarded card is not in the graveyard")
        void noOpWhenNotInGraveyard() {
            Permanent necro = createPermanent("Necropotence");
            var effect = new ExileDiscardedCardFromGraveyardEffect();
            Card discarded = createCard("Grizzly Bears");
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(necro, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.getPlayerExiledCards(player1Id)).isEmpty();
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — ScryEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — ScryEffect")
    class ControllerDiscardScry {

        @Test
        @DisplayName("queues a scry triggered ability for the discarding player and returns true")
        void queuesScryTrigger() {
            Permanent curator = createPermanent("Curator of Mysteries");
            var effect = new ScryEffect(1);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(curator, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(curator.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isInstanceOf(ScryEffect.class);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS - ExileTopCardMayPlayThisTurnEffect")
    class ControllerDiscardExileTopCard {

        @Test
        @DisplayName("queues an exile-and-play trigger for the controller")
        void queuesExileTopCardTrigger() {
            Permanent pyre = createPermanent("Pyre of the World Tree");
            var effect = new ExileTopCardMayPlayThisTurnEffect(false);
            Card discarded = createCard("Forest");
            discarded.setType(CardType.LAND);
            var ctx = new TriggerContext.Discard(player1Id, discarded);

            boolean result = registry.dispatch(
                    match(pyre, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(pyre.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — BoostSelfEffect")
    class ControllerDiscardSelfBoost {

        @Test
        @DisplayName("queues a self-boost triggered ability carrying the source permanent and returns true")
        void queuesSelfBoostTrigger() {
            Permanent hekma = createPermanent("Hekma Sentinels");
            var effect = new BoostSelfEffect(1, 1);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(hekma, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(hekma.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isInstanceOf(BoostSelfEffect.class);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — DealDamageToPlayersEffect")
    class ControllerDiscardDamageToEachOpponent {

        @Test
        @DisplayName("queues damage to each opponent with the source permanent and returns true")
        void queuesDamageTrigger() {
            Permanent buccaneer = createPermanent("Glint-Horn Buccaneer");
            var effect = new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(buccaneer, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(buccaneer.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — CreateTokenEffect")
    class ControllerDiscardCreateToken {

        @Test
        @DisplayName("queues a token-creation trigger for the controller")
        void queuesTokenCreationTrigger() {
            Permanent urza = createPermanent("Urza, Powerstone Prodigy");
            var effect = CreateTokenEffect.ofPowerstoneToken(new Fixed(1));
            var ctx = new TriggerContext.Discard(player1Id, createCard("Spellbook"));

            boolean result = registry.dispatch(
                    match(urza, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(urza.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — DealDamageToPlayersEffect")
    class ControllerDiscardEventDamageToEachOpponent {

        @Test
        @DisplayName("queues damage using the number of cards discarded in the event")
        void queuesDamageTriggerWithDiscardedCount() {
            Permanent artillerist = createPermanent("Magmakin Artillerist");
            var effect = new DealDamageToPlayersEffect(
                    new com.github.laxika.magicalvibes.model.amount.EventValue(), DamageRecipient.EACH_OPPONENT);
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(artillerist, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARD_EVENT, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(artillerist.getId());
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — AwardManaEffect")
    class ControllerDiscardAwardMana {

        @Test
        @DisplayName("queues a mana trigger for the controller")
        void queuesManaTrigger() {
            Permanent mishra = createPermanent("Mishra, Excavation Prodigy");
            var effect = new AwardManaEffect(ManaColor.RED, 2);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Spellbook"));

            boolean result = registry.dispatch(
                    match(mishra, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(mishra.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — PutCountersOnSelfEffect")
    class ControllerDiscardEventSelfCounters {

        @Test
        @DisplayName("queues counters using the number of cards discarded in the event")
        void queuesCounterTriggerWithDiscardedCount() {
            Permanent mako = createPermanent("Marauding Mako");
            var effect = new PutCountersOnSelfEffect(
                    CounterType.PLUS_ONE_PLUS_ONE,
                    new com.github.laxika.magicalvibes.model.amount.EventValue());
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(mako, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARD_EVENT, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(mako.getId());
            assertThat(entry.getEventValue()).isEqualTo(3);
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_CYCLES — PutCountersOnSelfEffect")
    class CycleSelfCounters {

        @Test
        @DisplayName("queues a counter trigger with the source permanent")
        void queuesCounterTrigger() {
            Permanent aura = createPermanent("Withering Hex");
            var effect = new PutCountersOnSelfEffect(CounterType.PLAGUE);
            var ctx = new TriggerContext.Cycle(player1Id, createCard("Censor"));

            boolean result = registry.dispatch(
                    match(aura, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_CYCLES, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — ExileTopCardsMayPlayUntilNextEndStepEffect")
    class ControllerDiscardEventExileTopCard {

        @Test
        @DisplayName("queues one top-card exile trigger for the discard event")
        void queuesTopCardExileTrigger() {
            Permanent inti = createPermanent("Inti, Seneschal of the Sun");
            var effect = new ExileTopCardsMayPlayUntilNextEndStepEffect(1);
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(inti, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARD_EVENT, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(inti.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — SequenceEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — SequenceEffect")
    class ControllerDiscardSequence {

        @Test
        @DisplayName("queues one atomic triggered ability carrying the source permanent and returns true")
        void queuesSequenceTrigger() {
            Permanent survivor = createPermanent("Cunning Survivor");
            var effect = SequenceEffect.of(new BoostSelfEffect(1, 0), new MakeCreatureUnblockableEffect(true));
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(survivor, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(survivor.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isInstanceOf(SequenceEffect.class);
        }
    }

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — SequenceEffect")
    class OpponentDiscardSequence {

        @Test
        @DisplayName("queues one atomic triggered ability for an opponent discard")
        void queuesSequenceTrigger() {
            Permanent nocturnus = createPermanent("Abyssal Nocturnus");
            var effect = SequenceEffect.of(
                    new BoostSelfEffect(2, 2),
                    new GrantKeywordEffect(Keyword.FEAR, GrantScope.SELF));
            var ctx = new TriggerContext.Discard(player2Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(nocturnus, player1Id, effect),
                    EffectSlot.ON_OPPONENT_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(nocturnus.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isEqualTo(effect);
        }
    }

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARD_EVENT — SequenceEffect")
    class ControllerDiscardEventSequence {

        @Test
        @DisplayName("queues a target choice carrying the discard-event count")
        void queuesTargetChoiceWithDiscardedCount() {
            Permanent captain = createPermanent("Captain Howler, Sea Scourge");
            var effect = SequenceEffect.of(
                    new BoostTargetCreatureEffect(2, 0),
                    new RegisterDelayedWatchedCreaturesCombatDamageEffect(List.of(new DrawCardEffect(1))));
            var ctx = new TriggerContext.DiscardEvent(player1Id, 3);

            boolean result = registry.dispatch(
                    match(captain, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARD_EVENT, effect, ctx);

            assertThat(result).isTrue();
            PermanentChoiceContext.DiscardControllerTriggerTarget pending =
                    gd.peekPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
            assertThat(pending.discardedCount()).isEqualTo(3);
            assertThat(pending.effects()).containsExactly(effect);
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — PutCounterOnEachMatchingPermanentEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — PutCounterOnEachMatchingPermanentEffect")
    class ControllerDiscardPutCounters {

        @Test
        @DisplayName("queues a put-counters triggered ability carrying the source permanent and returns true")
        void queuesPutCountersTrigger() {
            Permanent archfiend = createPermanent("Archfiend of Ifnir");
            var effect = new PutCounterOnEachMatchingPermanentEffect(
                    CounterType.MINUS_ONE_MINUS_ONE, 1,
                    new PermanentIsCreaturePredicate(), EachPermanentScope.ALL_PLAYERS);
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(archfiend, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(archfiend.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first()
                    .isInstanceOf(PutCounterOnEachMatchingPermanentEffect.class);
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — BoostTargetCreatureEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — BoostTargetCreatureEffect")
    class ControllerDiscardBoostTargetCreature {

        @Test
        @DisplayName("queues a target-creature choice carrying the source permanent and returns true")
        void queuesTargetChoice() {
            Permanent sphinx = createPermanent("Ominous Sphinx");
            var effect = new BoostTargetCreatureEffect(-2, 0, new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(sphinx, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.hasPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class)).isTrue();
            PermanentChoiceContext.DiscardControllerTriggerTarget pending =
                    gd.peekPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
            assertThat(pending.controllerId()).isEqualTo(player1Id);
            assertThat(pending.sourcePermanentId()).isEqualTo(sphinx.getId());
            assertThat(pending.effects()).hasSize(1).first().isInstanceOf(BoostTargetCreatureEffect.class);
        }
    }

    // ===== ON_CONTROLLER_DISCARDS — MayPayManaEffect =====

    @Nested
    @DisplayName("ON_CONTROLLER_DISCARDS — MayPayManaEffect")
    class ControllerDiscardMayPayMana {

        @Test
        @DisplayName("queues a may-pay triggered ability for the discarding player and returns true")
        void queuesMayPayManaTrigger() {
            Permanent drakeHaven = createPermanent("Drake Haven");
            var effect = new MayPayManaEffect("{1}", new ScryEffect(1), "Pay {1}?");
            var ctx = new TriggerContext.Discard(player1Id, createCard("Grizzly Bears"));

            boolean result = registry.dispatch(
                    match(drakeHaven, player1Id, effect),
                    EffectSlot.ON_CONTROLLER_DISCARDS, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(drakeHaven.getId());
            assertThat(entry.getEffectsToResolve()).hasSize(1).first().isInstanceOf(MayPayManaEffect.class);
        }
    }
}
