package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.LandsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersByAmountEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.NoteControllerLifeTotalEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattlefieldPlacementServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogService gameLogService;
    @Mock private PlayerInputService playerInputService;
    @Mock private PermanentCopierService permanentCopierService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private GraveyardTargetingService graveyardTargetingService;
    @Mock private ETBTokenTargetService etbTokenTargetService;
    @Mock private com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private com.github.laxika.magicalvibes.service.effect.normalfx.BecomeDayAsEntersEffectHandler becomeDayAsEntersEffectHandler;

    private BattlefieldPlacementService service;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        PredicateEvaluationService predicateEvaluationService = new PredicateEvaluationService(gameQueryService);
        ConditionEvaluationService conditionEvaluationService = new ConditionEvaluationService(
                gameQueryService, predicateEvaluationService);
        service = createPlacementService(gameQueryService, predicateEvaluationService, conditionEvaluationService);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        lenient().when(gameQueryService.replaceCounters(any(), any(), any(), any(), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(4));
        lenient().when(gameQueryService.computeStaticBonus(any(), any()))
                .thenReturn(GameQueryService.StaticBonus.NONE);
    }

    private BattlefieldPlacementService createPlacementService(
            GameQueryService queryService,
            PredicateEvaluationService predicateService,
            ConditionEvaluationService conditionService) {
        AmountEvaluationService amountService = new AmountEvaluationService(predicateService, queryService);
        var counterSupport = new com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport(
                queryService, predicateService, gameLogService, playerInputService);
        return new BattlefieldPlacementService(
                queryService, gameLogService, playerInputService, permanentCopierService,
                triggerCollectionService, amountService, conditionService, predicateService,
                counterSupport, graveyardService, permanentRemovalService, becomeDayAsEntersEffectHandler);
    }

    private void putPermanentOntoBattlefield(
            BattlefieldPlacementService target, GameData gameData, UUID controllerId, Permanent permanent) {
        target.place(gameData, new BattlefieldEntryRequest(controllerId, permanent,
                target.snapshotEnterTappedTypes(gameData), List.of(), 0, false, List.of()));
    }

    private void putPermanentOntoBattlefield(
            BattlefieldPlacementService target, GameData gameData, UUID controllerId, Permanent permanent,
            int xValue, boolean kicked) {
        target.place(gameData, new BattlefieldEntryRequest(controllerId, permanent,
                target.snapshotEnterTappedTypes(gameData), List.of(), xValue, kicked, List.of()));
    }

    private void putPermanentOntoBattlefield(
            BattlefieldPlacementService target, GameData gameData, UUID controllerId, Permanent permanent,
            Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered) {
        target.place(gameData, new BattlefieldEntryRequest(controllerId, permanent,
                enterTappedTypes, simultaneouslyEntered, 0, false, List.of()));
    }

    @Test
    @DisplayName("Notes the controller's life total as the permanent enters")
    void notesControllerLifeTotalAsPermanentEnters() {
        service.setNoteControllerLifeTotalEffectHandler(
                new com.github.laxika.magicalvibes.service.effect.normalfx.NoteControllerLifeTotalEffectHandler());
        gd.playerLifeTotals.put(player1Id, 17);

        Card card = new Card();
        card.setName("Life Note");
        card.setType(CardType.ENCHANTMENT);
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new NoteControllerLifeTotalEffect());
        Permanent entering = new Permanent(card);

        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.getChosenNumber()).isEqualTo(17);
    }

    @Test
    @DisplayName("Enters tapped when controller does not control a matching permanent")
    void entersTappedWhenPredicateNotSatisfied() {
        var predicate = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SWAMP, CardSubtype.MOUNTAIN));
        Card land = new Card();
        land.setName("Dragonskull Summit");
        land.setType(CardType.LAND);
        land.addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(0, predicate), new EntersTappedEffect()));
        Permanent entering = new Permanent(land);

        // Empty battlefield — controls zero matching permanents, so it enters tapped.
        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when controller controls a matching permanent")
    void entersUntappedWhenPredicateSatisfied() {
        var predicate = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SWAMP, CardSubtype.MOUNTAIN));
        Card land = new Card();
        land.setName("Dragonskull Summit");
        land.setType(CardType.LAND);
        land.addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(0, predicate), new EntersTappedEffect()));
        Permanent entering = new Permanent(land);

        // Controller already controls a matching Swamp, so the land enters untapped.
        Card swamp = new Card();
        swamp.setName("Swamp");
        swamp.setType(CardType.LAND);
        swamp.setSubtypes(List.of(CardSubtype.SWAMP));
        gd.playerBattlefields.get(player1Id).add(new Permanent(swamp));

        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.isTapped()).isFalse();
    }

    // ===== "Enters with … counters" replacement effects (CR 614.1c / 614.12) =====

    private Permanent enteringWithEffect(com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
        Card card = new Card();
        card.setName("Entering Permanent");
        card.setType(CardType.ARTIFACT);
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
        return new Permanent(card);
    }

    @Test
    @DisplayName("Enters with a fixed number of counters")
    void entersWithFixedCounters() {
        Permanent entering = enteringWithEffect(
                new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(3)));

        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters with X counters read from the spell's cast context")
    void entersWithXCounters() {
        Permanent entering = enteringWithEffect(
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        putPermanentOntoBattlefield(service, gd, player1Id, entering, 4, false);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters with 0 counters when not cast (X defaults to 0)")
    void entersWithZeroCountersWhenNotCast() {
        Permanent entering = enteringWithEffect(
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Kicked conditional applies counters only when the spell was kicked")
    void kickedConditionalCounters() {
        Permanent kicked = enteringWithEffect(new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
        Permanent notKicked = enteringWithEffect(new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));

        putPermanentOntoBattlefield(service, gd, player1Id, kicked, 0, true);
        putPermanentOntoBattlefield(service, gd, player1Id, notKicked, 0, false);

        assertThat(kicked.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(notKicked.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Raid conditional applies counters only when the controller attacked this turn")
    void raidConditionalCounters() {
        Permanent entering = enteringWithEffect(new ConditionalEffect(new Raid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
        putPermanentOntoBattlefield(service, gd, player1Id, entering);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        gd.playersDeclaredAttackersThisTurn.add(player1Id);
        Permanent enteringAfterRaid = enteringWithEffect(new ConditionalEffect(new Raid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
        putPermanentOntoBattlefield(service, gd, player1Id, enteringAfterRaid);
        assertThat(enteringAfterRaid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with a counter for each creature that died this turn (all players)")
    void entersWithCreatureDeathCounters() {
        UUID player2Id = UUID.randomUUID();
        gd.creatureDeathCountThisTurn.put(player1Id, 2);
        gd.creatureDeathCountThisTurn.put(player2Id, 1);
        gd.orderedPlayerIds.add(player2Id);
        Permanent entering = enteringWithEffect(new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));

        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("CantHaveCountersEffect prevents enters-with counters")
    void cantHaveCountersPreventsEntersWithCounters() {
        Permanent entering = enteringWithEffect(
                new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(3)));
        TestCards.mutableCard(entering).addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
        when(gameQueryService.cantHaveCountersForController(gd, entering, player1Id)).thenReturn(true);

        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Conditional battlefield entry counter effects apply when their condition is met")
    void conditionalBattlefieldEntryCountersApply() {
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        gd.lifeLostThisTurn.put(opponentId, 1);
        when(gameQueryService.doublePlusOnePlusOneCounters(any(), any(), any(), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(3));

        Card sourceCard = new Card();
        sourceCard.setName("Conditional Counter Source");
        sourceCard.setType(CardType.CREATURE);
        sourceCard.setSubtypes(List.of(CardSubtype.VAMPIRE));
        sourceCard.addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentLostLifeThisTurn(1),
                new ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype.VAMPIRE, 1)));
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

        Card enteringCard = new Card();
        enteringCard.setName("Entering Vampire");
        enteringCard.setType(CardType.CREATURE);
        enteringCard.setSubtypes(List.of(CardSubtype.VAMPIRE));
        Permanent entering = new Permanent(enteringCard);

        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dynamic battlefield entry counters use the number of lands entered this turn")
    void dynamicBattlefieldEntryCountersUseLandsEnteredThisTurn() {
        when(gameQueryService.doublePlusOnePlusOneCounters(any(), any(), any(), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(3));

        Card sourceCard = new Card();
        sourceCard.setName("Dynamic Counter Source");
        sourceCard.setType(CardType.ENCHANTMENT);
        sourceCard.addEffect(EffectSlot.STATIC,
                new ControlledPermanentsEnterWithAdditionalCountersByAmountEffect(
                        new PermanentIsCreaturePredicate(), new LandsEnteredBattlefieldThisTurn()));
        gd.playerBattlefields.get(player1Id).add(new Permanent(sourceCard));

        Card landOne = new Card();
        landOne.setType(CardType.LAND);
        Card landTwo = new Card();
        landTwo.setType(CardType.LAND);
        gd.permanentsEnteredBattlefieldThisTurn.put(player1Id,
                new ArrayList<>(List.of(landOne, landTwo)));

        Card enteringCard = new Card();
        enteringCard.setName("Entering Creature");
        enteringCard.setType(CardType.CREATURE);
        Permanent entering = new Permanent(enteringCard);
        when(gameQueryService.isCreature(gd, entering)).thenReturn(true);

        putPermanentOntoBattlefield(service, gd, player1Id, entering);

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    /**
     * Tests for {@link BattlefieldPlacementService#permanentWouldHaveSubtype} — the CR 614.12
     * replacement-effect lookahead that determines what subtypes a permanent would have
     * on the battlefield before it actually enters.
     *
     * <p>Builds its own service graph around a real {@link StaticEffectHandlerRegistry} with
     * manually registered handlers, rather than the mocked {@link GameQueryService} the outer
     * class uses — the lookahead's whole job is to run the real static-bonus computation.
     */
    @Nested
    @DisplayName("permanentWouldHaveSubtype — CR 614.12 lookahead")
    class PermanentWouldHaveSubtype {

        private StaticEffectHandlerRegistry realRegistry;
        private BattlefieldPlacementService lookaheadService;
        private GameData lookaheadGameData;
        private UUID playerId;
        private UUID opponentId;

        @BeforeEach
        void setUp() {
            realRegistry = new StaticEffectHandlerRegistry();
            realRegistry.register(GrantSubtypeEffect.class, (context, effect, accumulator) -> {
                var grant = (GrantSubtypeEffect) effect;
                boolean matches = switch (grant.scope()) {
                    case OWN_CREATURES, OWN_PERMANENTS -> context.targetOnSameBattlefield();
                    case ALL_CREATURES, ALL_PERMANENTS -> true;
                    case OPPONENT_CREATURES -> !context.targetOnSameBattlefield();
                    default -> false;
                };
                if (matches) {
                    accumulator.addGrantedSubtype(grant.subtype());
                }
            });
            realRegistry.register(GrantKeywordEffect.class, (context, effect, accumulator) -> {
                var grant = (GrantKeywordEffect) effect;
                boolean matches = switch (grant.scope()) {
                    case OWN_CREATURES, OWN_PERMANENTS -> context.targetOnSameBattlefield();
                    case ALL_CREATURES, ALL_PERMANENTS -> true;
                    default -> false;
                };
                if (matches) {
                    accumulator.addKeywords(grant.keywords());
                }
            });

            GameQueryService lookaheadGqs = new GameQueryService(realRegistry);
            PredicateEvaluationService lookaheadEvaluator = new PredicateEvaluationService(lookaheadGqs);
            ReflectionTestUtils.setField(lookaheadGqs, "predicateEvaluationService", lookaheadEvaluator);
            LayerSystemService lookaheadLayerSystem = new LayerSystemService();
            ReflectionTestUtils.setField(lookaheadLayerSystem, "predicateEvaluationService", lookaheadEvaluator);
            ReflectionTestUtils.setField(lookaheadLayerSystem, "staticEffectRegistry", realRegistry);
            ReflectionTestUtils.setField(lookaheadLayerSystem, "gameQueryService", lookaheadGqs);
            ReflectionTestUtils.setField(lookaheadGqs, "layerSystemService", lookaheadLayerSystem);

            ConditionEvaluationService lookaheadConditions =
                    new ConditionEvaluationService(lookaheadGqs, lookaheadEvaluator);
            lookaheadService = createPlacementService(lookaheadGqs, lookaheadEvaluator, lookaheadConditions);

            playerId = UUID.randomUUID();
            opponentId = UUID.randomUUID();
            lookaheadGameData = new GameData(UUID.randomUUID(), "test", playerId, "TestPlayer");
            lookaheadGameData.orderedPlayerIds.add(playerId);
            lookaheadGameData.orderedPlayerIds.add(opponentId);
            lookaheadGameData.playerBattlefields.put(playerId, Collections.synchronizedList(new ArrayList<>()));
            lookaheadGameData.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));
        }

        private Card createCreatureCard(String name, List<CardSubtype> subtypes) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            card.setSubtypes(subtypes);
            return card;
        }

        private Card createCreatureCardWithKeywords(String name, List<CardSubtype> subtypes, Keyword... keywords) {
            Card card = createCreatureCard(name, subtypes);
            card.setKeywords(java.util.EnumSet.copyOf(java.util.Set.of(keywords)));
            return card;
        }

        @Test
        @DisplayName("Returns true when permanent has the subtype naturally")
        void naturalSubtype() {
            Permanent entering = new Permanent(createCreatureCard("Elite Vanguard", List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
        }

        @Test
        @DisplayName("Returns false when permanent does not have the subtype and no effects grant it")
        void noMatchingSubtype() {
            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isFalse();
        }

        @Test
        @DisplayName("Returns true when permanent has Changeling keyword (all creature subtypes)")
        void changelingHasAllCreatureSubtypes() {
            Permanent entering = new Permanent(
                    createCreatureCardWithKeywords("Changeling Outcast", List.of(CardSubtype.SHAPESHIFTER), Keyword.CHANGELING));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.GOBLIN)).isTrue();
        }

        @Test
        @DisplayName("Changeling does not match non-creature subtypes (e.g., Equipment, Aura)")
        void changelingDoesNotMatchNonCreatureSubtypes() {
            Permanent entering = new Permanent(
                    createCreatureCardWithKeywords("Changeling Outcast", List.of(CardSubtype.SHAPESHIFTER), Keyword.CHANGELING));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.EQUIPMENT)).isFalse();
        }

        @Test
        @DisplayName("Returns true when a battlefield permanent grants the subtype to own creatures")
        void subtypeGrantedByBattlefieldPermanent() {
            Card xenograftCard = new Card();
            xenograftCard.setName("Xenograft");
            xenograftCard.setType(CardType.ENCHANTMENT);
            xenograftCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent xenograft = new Permanent(xenograftCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(xenograft);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
        }

        @Test
        @DisplayName("Subtype lookahead does not mutate the battlefield iteration for additional counters")
        void additionalCounterSubtypeLookaheadDoesNotMutateBattlefieldIteration() {
            Card counterSourceCard = new Card();
            counterSourceCard.setName("Counter Source");
            counterSourceCard.setType(CardType.CREATURE);
            counterSourceCard.addEffect(EffectSlot.STATIC,
                    new ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype.WIZARD, 1));
            Permanent counterSource = new Permanent(counterSourceCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(counterSource);

            Card subtypeSourceCard = new Card();
            subtypeSourceCard.setName("Subtype Source");
            subtypeSourceCard.setType(CardType.ENCHANTMENT);
            subtypeSourceCard.addEffect(EffectSlot.STATIC,
                    new GrantSubtypeEffect(CardSubtype.WIZARD, GrantScope.OWN_CREATURES));
            Permanent subtypeSource = new Permanent(subtypeSourceCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(subtypeSource);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            putPermanentOntoBattlefield(lookaheadService, lookaheadGameData, playerId, entering);

            assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
            assertThat(lookaheadGameData.playerBattlefields.get(playerId)).containsExactly(
                    counterSource, subtypeSource, entering);
        }

        @Test
        @DisplayName("Chosen-subtype additional-counter source applies only to the type its source chose")
        void chosenSubtypeAdditionalCounterSourceUsesSourceChoice() {
            Card counterSourceCard = new Card();
            counterSourceCard.setName("Chosen Counter Source");
            counterSourceCard.setType(CardType.CREATURE);
            counterSourceCard.addEffect(EffectSlot.STATIC,
                    ControlledCreaturesEnterWithAdditionalCountersEffect.ofChosenSubtype(1));
            Permanent counterSource = new Permanent(counterSourceCard);
            counterSource.setChosenSubtype(CardSubtype.BEAR);
            lookaheadGameData.playerBattlefields.get(playerId).add(counterSource);

            Permanent bear = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));
            putPermanentOntoBattlefield(lookaheadService, lookaheadGameData, playerId, bear);
            assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();

            Permanent goblin = new Permanent(createCreatureCard("Goblin Piker", List.of(CardSubtype.GOBLIN)));
            putPermanentOntoBattlefield(lookaheadService, lookaheadGameData, playerId, goblin);
            assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }

        @Test
        @DisplayName("Chosen-subtype additional-counter source grants nothing before a type is chosen")
        void chosenSubtypeAdditionalCounterSourceInertWithoutChoice() {
            Card counterSourceCard = new Card();
            counterSourceCard.setName("Chosen Counter Source");
            counterSourceCard.setType(CardType.CREATURE);
            counterSourceCard.addEffect(EffectSlot.STATIC,
                    ControlledCreaturesEnterWithAdditionalCountersEffect.ofChosenSubtype(1));
            lookaheadGameData.playerBattlefields.get(playerId).add(new Permanent(counterSourceCard));

            Permanent bear = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));
            putPermanentOntoBattlefield(lookaheadService, lookaheadGameData, playerId, bear);

            assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }

        @Test
        @DisplayName("Multiple additional-counter sources matching a looked-ahead subtype all apply")
        void multipleAdditionalCounterSourcesStack() {
            Card firstSourceCard = new Card();
            firstSourceCard.setName("First Counter Source");
            firstSourceCard.setType(CardType.CREATURE);
            firstSourceCard.addEffect(EffectSlot.STATIC,
                    new ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype.WIZARD, 1));
            lookaheadGameData.playerBattlefields.get(playerId).add(new Permanent(firstSourceCard));

            Card secondSourceCard = new Card();
            secondSourceCard.setName("Second Counter Source");
            secondSourceCard.setType(CardType.CREATURE);
            secondSourceCard.addEffect(EffectSlot.STATIC,
                    new ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype.WIZARD, 2));
            lookaheadGameData.playerBattlefields.get(playerId).add(new Permanent(secondSourceCard));

            Card subtypeSourceCard = new Card();
            subtypeSourceCard.setName("Subtype Source");
            subtypeSourceCard.setType(CardType.ENCHANTMENT);
            subtypeSourceCard.addEffect(EffectSlot.STATIC,
                    new GrantSubtypeEffect(CardSubtype.WIZARD, GrantScope.OWN_CREATURES));
            lookaheadGameData.playerBattlefields.get(playerId).add(new Permanent(subtypeSourceCard));

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            putPermanentOntoBattlefield(lookaheadService, lookaheadGameData, playerId, entering);

            assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        }

        @Test
        @DisplayName("Opponent's subtype-granting effect with OWN_CREATURES scope does not affect your creatures")
        void opponentOwnCreaturesScopeDoesNotAffect() {
            Card xenograftCard = new Card();
            xenograftCard.setName("Xenograft");
            xenograftCard.setType(CardType.ENCHANTMENT);
            xenograftCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent xenograft = new Permanent(xenograftCard);
            lookaheadGameData.playerBattlefields.get(opponentId).add(xenograft);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isFalse();
        }

        @Test
        @DisplayName("ALL_CREATURES scope grants subtype regardless of controller")
        void allCreaturesScopeAffectsEveryone() {
            Card conspiracyCard = new Card();
            conspiracyCard.setName("Conspiracy");
            conspiracyCard.setType(CardType.ENCHANTMENT);
            conspiracyCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.ALL_CREATURES));
            Permanent conspiracy = new Permanent(conspiracyCard);
            lookaheadGameData.playerBattlefields.get(opponentId).add(conspiracy);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
        }

        @Test
        @DisplayName("Returns true when a battlefield permanent grants Changeling to own creatures")
        void changelingGrantedByStaticEffect() {
            Card maskCard = new Card();
            maskCard.setName("Maskwood Nexus");
            maskCard.setType(CardType.ARTIFACT);
            maskCard.addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.OWN_CREATURES));
            Permanent mask = new Permanent(maskCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(mask);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.GOBLIN)).isTrue();
        }

        @Test
        @DisplayName("Simultaneously-entered permanent with subtype grant is excluded from lookahead")
        void simultaneouslyEnteredExcludedFromLookahead() {
            Card xenograftCard = new Card();
            xenograftCard.setName("Xenograft");
            xenograftCard.setType(CardType.ENCHANTMENT);
            xenograftCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent xenograft = new Permanent(xenograftCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(xenograft);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(xenograft), CardSubtype.HUMAN)).isFalse();
        }

        @Test
        @DisplayName("Pre-existing battlefield permanent IS visible even when other simultaneous entries are excluded")
        void preExistingPermanentVisibleDespiteSimultaneousExclusion() {
            Card xenograftCard = new Card();
            xenograftCard.setName("Xenograft");
            xenograftCard.setType(CardType.ENCHANTMENT);
            xenograftCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent preExistingXenograft = new Permanent(xenograftCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(preExistingXenograft);

            Permanent simultaneousCreature = new Permanent(createCreatureCard("Elvish Mystic", List.of(CardSubtype.ELF)));
            lookaheadGameData.playerBattlefields.get(playerId).add(simultaneousCreature);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(simultaneousCreature), CardSubtype.HUMAN)).isTrue();
        }

        @Test
        @DisplayName("Empty simultaneous list means all battlefield permanents are visible")
        void emptySimultaneousListMeansAllVisible() {
            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadService.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isFalse();
        }

        @Test
        @DisplayName("Lookahead restores battlefield exactly: entering removed, excluded re-added")
        void lookaheadCleansUpTemporaryPermanents() {
            Permanent simEntry = new Permanent(createCreatureCard("Another Bear", List.of(CardSubtype.BEAR)));
            lookaheadGameData.playerBattlefields.get(playerId).add(simEntry);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            List<Permanent> bfBefore = new ArrayList<>(lookaheadGameData.playerBattlefields.get(playerId));

            lookaheadService.permanentWouldHaveSubtype(lookaheadGameData, entering, playerId, List.of(simEntry), CardSubtype.HUMAN);

            assertThat(lookaheadGameData.playerBattlefields.get(playerId)).containsExactlyElementsOf(bfBefore);
        }

        @Test
        @DisplayName("Lookahead restores battlefield even when static bonus computation fails")
        void lookaheadCleansUpOnException() {
            realRegistry.register(GrantSubtypeEffect.class, (context, effect, accumulator) -> {
                throw new RuntimeException("simulated failure");
            });

            Card badCard = new Card();
            badCard.setName("Bad Enchantment");
            badCard.setType(CardType.ENCHANTMENT);
            badCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            lookaheadGameData.playerBattlefields.get(playerId).add(new Permanent(badCard));

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));
            List<Permanent> bfBefore = new ArrayList<>(lookaheadGameData.playerBattlefields.get(playerId));

            try {
                lookaheadService.permanentWouldHaveSubtype(lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN);
            } catch (RuntimeException ignored) {
                // expected
            }

            assertThat(lookaheadGameData.playerBattlefields.get(playerId)).containsExactlyElementsOf(bfBefore);
        }

        /**
         * The excluded permanents sit in the middle of the battlefield, not at the end. Restoring
         * by re-appending them would leave the list reordered, and battlefield order is
         * load-bearing (CR 613.7 equal-timestamp tiebreak, trigger stack order, and the index the
         * wire protocol addresses permanents by). No production caller passes a non-empty
         * simultaneous batch yet, so this guards the parameter for whoever first wires it up.
         */
        @Test
        @DisplayName("Lookahead restores battlefield order when excluded permanents sit mid-list")
        void lookaheadRestoresOrderWithExcludedPermanentsMidList() {
            Permanent first = new Permanent(createCreatureCard("First Bear", List.of(CardSubtype.BEAR)));
            Permanent excludedA = new Permanent(createCreatureCard("Excluded A", List.of(CardSubtype.BEAR)));
            Permanent middle = new Permanent(createCreatureCard("Middle Bear", List.of(CardSubtype.BEAR)));
            Permanent excludedB = new Permanent(createCreatureCard("Excluded B", List.of(CardSubtype.BEAR)));
            Permanent last = new Permanent(createCreatureCard("Last Bear", List.of(CardSubtype.BEAR)));

            List<Permanent> battlefield = lookaheadGameData.playerBattlefields.get(playerId);
            battlefield.addAll(List.of(first, excludedA, middle, excludedB, last));

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            lookaheadService.permanentWouldHaveSubtype(lookaheadGameData, entering, playerId,
                    List.of(excludedA, excludedB), CardSubtype.HUMAN);

            assertThat(battlefield).containsExactly(first, excludedA, middle, excludedB, last);
        }

        /**
         * CR 614.12 / official ruling: "If Bramblewood Paragon enters at the same time as another
         * Warrior (due to Living End, for example), that creature doesn't get a +1/+1 counter."
         * The source is physically on the battlefield by the time the batch's later members enter,
         * so it must be hidden for the replacement-effect window.
         */
        @Test
        @DisplayName("A counter source entering in the same batch does not grant the counter")
        void simultaneousCounterSourceDoesNotApply() {
            Permanent counterSource = counterSourcePermanent();
            List<Permanent> battlefield = lookaheadGameData.playerBattlefields.get(playerId);
            battlefield.add(counterSource);

            Permanent entering = new Permanent(createCreatureCard("Warrior Recruit", List.of(CardSubtype.WARRIOR)));

            putPermanentOntoBattlefield(lookaheadService,
                    lookaheadGameData, playerId, entering, Set.of(), List.of(counterSource));

            assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }

        /** Control for {@link #simultaneousCounterSourceDoesNotApply} — the same source already on
         *  the battlefield (not part of the batch) does grant the counter. */
        @Test
        @DisplayName("A counter source already on the battlefield still grants the counter")
        void preexistingCounterSourceStillApplies() {
            Permanent counterSource = counterSourcePermanent();
            lookaheadGameData.playerBattlefields.get(playerId).add(counterSource);

            Permanent entering = new Permanent(createCreatureCard("Warrior Recruit", List.of(CardSubtype.WARRIOR)));

            putPermanentOntoBattlefield(lookaheadService,
                    lookaheadGameData, playerId, entering, Set.of(), List.of());

            assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        }

        @Test
        @DisplayName("An enters-as-copy source entering in the same batch does not copy")
        void simultaneousEnterAsCopySourceDoesNotApply() {
            Card essenceCard = new Card();
            essenceCard.setName("Essence Source");
            essenceCard.setType(CardType.CREATURE);
            essenceCard.addEffect(EffectSlot.STATIC, new CreaturesEnterAsCopyOfSourceEffect());
            Permanent essence = new Permanent(essenceCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(essence);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            putPermanentOntoBattlefield(lookaheadService,
                    lookaheadGameData, playerId, entering, Set.of(), List.of(essence));

            verify(permanentCopierService, never()).applyCloneCopy(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Hiding the batch restores every battlefield in its original order")
        void hidingBatchRestoresBattlefieldOrder() {
            Permanent first = new Permanent(createCreatureCard("First Bear", List.of(CardSubtype.BEAR)));
            Permanent batchMate = counterSourcePermanent();
            Permanent last = new Permanent(createCreatureCard("Last Bear", List.of(CardSubtype.BEAR)));

            List<Permanent> battlefield = lookaheadGameData.playerBattlefields.get(playerId);
            battlefield.addAll(List.of(first, batchMate, last));

            Permanent opponentPermanent = new Permanent(createCreatureCard("Opposing Bear", List.of(CardSubtype.BEAR)));
            lookaheadGameData.playerBattlefields.get(opponentId).add(opponentPermanent);

            Permanent entering = new Permanent(createCreatureCard("Warrior Recruit", List.of(CardSubtype.WARRIOR)));

            putPermanentOntoBattlefield(lookaheadService,
                    lookaheadGameData, playerId, entering, Set.of(), List.of(batchMate));

            assertThat(battlefield).containsExactly(first, batchMate, last, entering);
            assertThat(lookaheadGameData.playerBattlefields.get(opponentId)).containsExactly(opponentPermanent);
        }

        private Permanent counterSourcePermanent() {
            Card sourceCard = new Card();
            sourceCard.setName("Counter Source");
            sourceCard.setType(CardType.CREATURE);
            sourceCard.addEffect(EffectSlot.STATIC,
                    new ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype.WARRIOR, 1));
            return new Permanent(sourceCard);
        }

        @Test
        @DisplayName("Lookahead restores mid-list order even when static bonus computation fails")
        void lookaheadRestoresOrderOnExceptionWithExcludedPermanents() {
            realRegistry.register(GrantSubtypeEffect.class, (context, effect, accumulator) -> {
                throw new RuntimeException("simulated failure");
            });

            Permanent first = new Permanent(createCreatureCard("First Bear", List.of(CardSubtype.BEAR)));
            Permanent excluded = new Permanent(createCreatureCard("Excluded Bear", List.of(CardSubtype.BEAR)));

            Card badCard = new Card();
            badCard.setName("Bad Enchantment");
            badCard.setType(CardType.ENCHANTMENT);
            badCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent thrower = new Permanent(badCard);

            List<Permanent> battlefield = lookaheadGameData.playerBattlefields.get(playerId);
            battlefield.addAll(List.of(first, excluded, thrower));

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            try {
                lookaheadService.permanentWouldHaveSubtype(lookaheadGameData, entering, playerId,
                        List.of(excluded), CardSubtype.HUMAN);
            } catch (RuntimeException ignored) {
                // expected
            }

            assertThat(battlefield).containsExactly(first, excluded, thrower);
        }
    }
}
