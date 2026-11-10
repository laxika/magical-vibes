package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TransformTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggerCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

/**
 * Unit tests for the "whenever [an object] enters" collector bodies the trigger pipeline hands its matches to
 * ({@link TriggerCollectionService#triggerZoneChange}), driving the {@link EnterTriggerCollectorService} handlers
 * through a real {@link TriggerCollectorRegistry} (mirrors {@code MiscTriggerCollectorServiceTest}'s setup). The tests
 * do the pipeline's matching themselves ({@code trigger}), and call the remaining choke-point collectors (evolve,
 * watchers, boons, emblems) where the old collector did both.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnterTriggerCollectorServiceTest {

    private static final GameQueryService.StaticBonus EMPTY_BONUS = new GameQueryService.StaticBonus(
            0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(),
            Set.of(), false, false, false, false, Set.of(), false, 0, 0, false, false);

    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TriggeredAbilityQueueService triggeredAbilityQueueService;
    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private TargetLegalityService targetLegalityService;
    @Mock private ValidTargetService validTargetService;
    @Mock private GameLogService gameLogService;
    @Mock private ETBTokenTargetService etbTokenTargetService;

    private TriggerCollectionService service;
    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;

    @Test
    void landDamageTriggerPreservesEnteringPermanentAndController() {
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        Card landCard = new Card();
        landCard.setName("Entering land");
        landCard.setType(CardType.LAND);
        Permanent land = new Permanent(landCard);
        gd.playerBattlefields.put(opponentId, new ArrayList<>(List.of(land)));
        addAllyCreatureTrigger(ZoneChangeWatch.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect(2,
                        com.github.laxika.magicalvibes.model.effect.DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        // The default collector records the land's controller as the land is found on the battlefield.
        when(gameQueryService.findPermanentController(gd, land.getId())).thenReturn(opponentId);

        enter(opponentId, landCard);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(land.getId());
        assertThat(gd.stack.getFirst().getTriggeringPermanentControllerId()).isEqualTo(opponentId);
    }

    @BeforeEach
    void setUp() {
        lenient().when(gameQueryService.computeStaticBonus(any(GameData.class), any(Permanent.class), isNull()))
                .thenReturn(EMPTY_BONUS);
        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(new EnterTriggerCollectorService(gameLogService, gameQueryService),
                registry);

        service = new TriggerCollectionService(registry, gameOutcomeService, playerInputService,
                triggeredAbilityQueueService, gameQueryService, predicateEvaluationService,
                targetLegalityService,
                validTargetService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService),
                new AmountEvaluationService(predicateEvaluationService, gameQueryService),
                gameLogService, etbTokenTargetService,
                new GrantedTriggeredAbilitySupport(gameQueryService),
                new GraveyardTargetingSupport());

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        lenient().when(gameQueryService.isCreature(eq(gd), any(Permanent.class)))
                .thenAnswer(invocation -> ((Permanent) invocation.getArgument(1)).getCard().hasType(CardType.CREATURE));
        lenient().when(gameQueryService.getEffectivePower(eq(gd), any(Permanent.class)))
                .thenAnswer(invocation -> ((Permanent) invocation.getArgument(1)).getCard().getPower());
        lenient().when(gameQueryService.getEffectiveToughness(eq(gd), any(Permanent.class)))
                .thenAnswer(invocation -> ((Permanent) invocation.getArgument(1)).getCard().getToughness());
        lenient().when(gameQueryService.findPermanentById(eq(gd), any(UUID.class))).thenAnswer(invocation -> {
            Permanent[] found = new Permanent[1];
            gd.forEachPermanent((ownerId, permanent) -> {
                if (permanent.getId().equals(invocation.getArgument(1))) {
                    found[0] = permanent;
                }
            });
            return found[0];
        });
    }

    private void addAllyCreatureTrigger(ZoneChangeWatch watch, CardEffect effect) {
        Card source = new Card();
        source.setName("Source");
        addWatched(source, watch, effect);
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));
    }

    /** Gives {@code card} a triggered ability of {@code effect} with the first trigger condition {@code watch} accepts. */
    private static void addWatched(Card card, ZoneChangeWatch watch, CardEffect effect) {
        ZoneChangeWatch.Key key = watch.keys().getFirst();
        card.addTrigger(new TriggerCondition(key.trigger(), AbilityZones.of(key.zone())),
                TriggerOptions.DEFAULT.withMultiplicity(key.multiplicity()), effect);
    }

    /**
     * {@code card} entering under {@code controllerId}'s control, as the trigger pipeline hands it to the old
     * collectors' bodies: every enters ability of a permanent or graveyard card whose relation and filter it meets.
     */
    private void enter(UUID controllerId, Card card) {
        trigger(controllerId, List.of(enteringPermanent(card)));
    }

    /** The tokens with ids {@code tokenIds} entering together under {@code controllerId}'s control. */
    private void enterTokens(UUID controllerId, List<UUID> tokenIds) {
        List<Permanent> tokens = new ArrayList<>();
        gd.forEachPermanent((ownerId, permanent) -> {
            if (tokenIds.contains(permanent.getId())) {
                tokens.add(permanent);
            }
        });
        trigger(controllerId, tokens);
    }

    private Permanent enteringPermanent(Card card) {
        Permanent[] found = {new Permanent(card)};
        gd.forEachPermanent((ownerId, permanent) -> {
            if (permanent.getCard() == card) {
                found[0] = permanent;
            }
        });
        return found[0];
    }

    private void trigger(UUID controllerId, List<Permanent> entering) {
        List<TriggerCandidate> candidates = new ArrayList<>();
        gd.forEachPermanent((ownerId, permanent) -> permanent.getCard().getTriggeredAbilities().forEach(ability ->
                candidates.add(new TriggerCandidate(permanent.getCard(), permanent, ownerId, Zone.BATTLEFIELD,
                        ability))));
        gd.playerGraveyards.forEach((ownerId, graveyard) -> graveyard.forEach(card ->
                card.getTriggeredAbilities().forEach(ability ->
                        candidates.add(new TriggerCandidate(card, null, ownerId, Zone.GRAVEYARD, ability)))));
        for (TriggerCandidate candidate : candidates) {
            for (TriggerCondition condition : candidate.ability().conditions()) {
                ZoneChangeWatch watch = ZoneChangeWatch.of(candidate.ability(), condition.trigger(), candidate.zone());
                if (watch == null || !watch.entering()) {
                    continue;
                }
                List<ZoneChangeEvent> events = entering.stream()
                        .filter(permanent -> relates((ZoneChangeTrigger) condition.trigger(), candidate, permanent,
                                controllerId))
                        .map(permanent -> new ZoneChangeEvent(permanent.getCard(), controllerId, Zone.HAND,
                                Zone.BATTLEFIELD, ZoneChangeCause.OTHER, new Permanent(permanent), controllerId))
                        .toList();
                List<CardEffect> effects = candidate.ability().effects().stream()
                        .map(EffectRegistration::effect).toList();
                if (events.isEmpty()) {
                    continue;
                }
                if (candidate.ability().options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE) {
                    service.triggerZoneChange(gd, watch, candidate, effects, events);
                } else {
                    events.forEach(event -> service.triggerZoneChange(gd, watch, candidate, effects, List.of(event)));
                }
            }
        }
    }

    /** The relation and type filters the trigger pipeline checks before an ability reaches the collectors' bodies. */
    private static boolean relates(ZoneChangeTrigger trigger, TriggerCandidate candidate, Permanent entering,
                                   UUID controllerId) {
        boolean self = candidate.sourcePermanent() != null
                && candidate.sourcePermanent().getId().equals(entering.getId());
        boolean controls = candidate.controllerId().equals(controllerId);
        boolean related = switch (trigger.relation()) {
            case THIS -> self;
            case YOU_CONTROL -> controls;
            case ANOTHER_YOU_CONTROL -> controls && !self;
            case ANOTHER -> !self;
            case OPPONENT_CONTROLS -> !controls;
            default -> true;
        };
        Card card = entering.getCard();
        PermanentPredicate filter = trigger.filter();
        boolean matches = filter == null
                || filter.equals(Triggers.CREATURE) && card.hasType(CardType.CREATURE)
                || filter.equals(Triggers.NONTOKEN_CREATURE) && card.hasType(CardType.CREATURE) && !card.isToken()
                || filter.equals(Triggers.ARTIFACT) && card.hasType(CardType.ARTIFACT)
                || filter.equals(Triggers.NONTOKEN_ARTIFACT) && card.hasType(CardType.ARTIFACT) && !card.isToken()
                || filter.equals(Triggers.LAND) && card.hasType(CardType.LAND)
                || filter.equals(Triggers.ENCHANTMENT) && card.hasType(CardType.ENCHANTMENT)
                || filter.equals(Triggers.TOKEN) && card.isToken()
                || filter.equals(Triggers.EQUIPMENT) && card.getSubtypes().contains(CardSubtype.EQUIPMENT);
        return related && matches;
    }

    private static Card enteringCreature(int power, int toughness) {
        Card entering = new Card();
        entering.setName("Entering Creature");
        entering.setType(CardType.CREATURE);
        entering.setPower(power);
        entering.setToughness(toughness);
        return entering;
    }

    @Test
    @DisplayName("Life-gain once-per-turn triggers fire only for the first life-gain event")
    void lifeGainOncePerTurnTriggerFiresOnlyOnce() {
        when(gameQueryService.computeStaticBonus(eq(gd), any(Permanent.class), isNull())).thenReturn(EMPTY_BONUS);
        CardEffect inner = new GainLifeEffect(1);
        Card source = new Card();
        source.setName("Source");
        source.addTrigger(Triggers.youGainLife(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), inner);
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));

        AtomicInteger dispatchCount = new AtomicInteger();
        registry.register(LifeWatch.ON_CONTROLLER_GAINS_LIFE, CardEffect.class,
                (match, dispatchedEffect, context) -> {
                    dispatchCount.incrementAndGet();
                    return true;
                });

        service.checkLifeGainTriggers(gd, player1Id, 3);
        service.checkLifeGainTriggers(gd, player1Id, 3);

        assertThat(dispatchCount).hasValue(1);
        assertThat(gd.keyedOncePerTurnTriggersFiredThisTurn.get(gd.playerBattlefields.get(player1Id).getFirst().getId()))
                .containsExactly(source.getTriggeredAbilities().getFirst().limitKey());
        assertThat(gd.oncePerTurnTriggersFiredThisTurn).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature scan skips non-creature (null toughness) entrants")
    void allyCreatureSkipsNonCreature() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        Card land = new Card();
        land.setName("Forest");
        land.setType(CardType.LAND);

        enter(player1Id, land);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, land, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-permanent player targets are chosen when the trigger is put on the stack")
    void allyPermanentPlayerTargetQueuesChoice() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new MillEffect(2, MillRecipient.TARGET_PLAYER));

        Card entering = new Card();
        entering.setName("Snow Permanent");
        entering.setType(CardType.LAND);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyPermanentEntersTriggers(gd, player1Id, entering);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)).isTrue();
    }

    @Test
    @DisplayName("Ally-creature ability queues its effect")
    void allyCreatureQueuesEffect() {
        Card source = new Card();
        source.setName("Garruk's Packleader");
        addWatched(source, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));
        Card entering = enteringCreature(4, 4);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(source);
    }

    @Test
    @DisplayName("Ally-creature scan does not trigger for the entering permanent itself")
    void allyCreatureDoesNotTriggerSelf() {
        Card entering = enteringCreature(4, 4);
        addWatched(entering, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature turn-scoped modal queues mode selection")
    void allyCreatureTurnScopedModalQueuesModeSelection() {
        Card source = new Card();
        source.setName("Gala Greeters");
        addWatched(source, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, ChooseOneEffect.notYetChosenThisTurn(List.of(
                        new ChooseOneEffect.ChooseOneOption("First mode", new GainLifeEffect(1)),
                        new ChooseOneEffect.ChooseOneOption("Second mode", new GainLifeEffect(1)))));
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card entering = enteringCreature(2, 2);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions)
                .filteredOn(PermanentChoiceContext.TriggeredModalTrigger.class::isInstance)
                .hasSize(1);
        var pending = gd.pendingInteractions.stream()
                .filter(PermanentChoiceContext.TriggeredModalTrigger.class::isInstance)
                .map(PermanentChoiceContext.TriggeredModalTrigger.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(pending.modesResetEachTurn()).isTrue();
        assertThat(pending.sourcePermanentId()).isEqualTo(sourcePermanent.getId());
    }

    @Test
    @DisplayName("Ally-creature trigger-time modal queues mode selection")
    void allyCreatureTriggerTimeModalQueuesModeSelection() {
        Card source = new Card();
        source.setName("Ayula");
        ChooseOneAtTriggerTimeEffect choice = new ChooseOneAtTriggerTimeEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("First mode", new GainLifeEffect(1)),
                        new ChooseOneEffect.ChooseOneOption("Second mode", new GainLifeEffect(1)))));
        addWatched(source, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, choice);
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).isEmpty();
        var pending = gd.pendingInteractions.stream()
                .filter(PermanentChoiceContext.TriggeredModalTrigger.class::isInstance)
                .map(PermanentChoiceContext.TriggeredModalTrigger.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(pending.effect()).isSameAs(choice.choice());
        assertThat(pending.sourcePermanentId()).isEqualTo(sourcePermanent.getId());
        assertThat(pending.triggeringCardId()).isEqualTo(entering.getId());
        assertThat(pending.triggeringPermanentId()).isEqualTo(enteringPermanent.getId());
        assertThat(pending.modesResetEachTurn()).isFalse();
    }

    @Test
    @DisplayName("Naban doubling duplicates each ally-creature trigger produced during the scan")
    void allyCreatureNabanDoubling() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));
        Card entering = enteringCreature(4, 4);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        when(gameQueryService.countETBExtraTriggers(gd, player1Id, player1Id, entering)).thenReturn(1);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 1);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Ally-creature \"you may transform it\" goes on the stack as written, the entering creature its triggering permanent (Vildin-Pack Alpha)")
    void allyCreatureMayTransformEntering() {
        MayEffect authored = new MayEffect(TransformTargetPermanentEffect.ofTriggeringPermanent(), "Transform it?");
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, authored);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertWrittenOnTheEntering(authored, enteringPermanent);
    }

    @Test
    @DisplayName("Self-or-ally may life gain equal to that creature's power goes on the stack as written, the entering creature its triggering permanent")
    void selfOrAllyFlyingGainLifeReadsTheTriggeringPermanent() {
        MayEffect authored = new MayEffect(new GainLifeEffect(new TriggeringPermanentPower()), "Gain life?");
        addAllyCreatureTrigger(ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD, authored);

        Card entering = enteringCreature(4, 1);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEffectsToResolve()).containsExactly(authored);
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(enteringPermanent.getId());
        assertThat(entry.getTriggeringCardId()).isEqualTo(entering.getId());
    }

    @Test
    @DisplayName("Ally-creature \"you may put counters on it\" goes on the stack as written, the entering creature its triggering permanent (Mighty Emergence)")
    void allyCreaturePutCountersOnEntering() {
        MayEffect authored = new MayEffect(PutCounterOnTargetPermanentEffect.onTriggeringPermanent(
                CounterType.PLUS_ONE_PLUS_ONE, 2), "Put two +1/+1 counters on it?");
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, authored);

        Card entering = enteringCreature(8, 8);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertWrittenOnTheEntering(authored, enteringPermanent);
    }

    @Test
    void nontokenCreatureCounterAndKeywordGrantShareOneNonTargetingAbility() {
        SequenceEffect authored = SequenceEffect.of(
                PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE, 1),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT));
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, authored);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertWrittenOnTheEntering(authored, enteringPermanent);
    }

    @Test
    void nontokenCreatureCounterTriggerExcludesTokens() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE, 1));
        Card entering = enteringCreature(2, 2);
        entering.setToken(true);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        enter(player1Id, entering);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature oil-counter trigger goes on the stack as written, the entering permanent its triggering permanent")
    void allyCreatureCounterTriggerTargetsEnteringPermanent() {
        PutCounterOnTargetPermanentEffect authored =
                PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.OIL, 1);
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, authored);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        enteringPermanent.setCounterCount(CounterType.OIL, 1);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertWrittenOnTheEntering(authored, enteringPermanent);
    }

    @Test
    @DisplayName("Ally-creature boost and haste go on the stack as written, the entrant the triggering permanent (Ogre Battledriver)")
    void allyCreatureBoostEntering() {
        SequenceEffect authored = SequenceEffect.of(
                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 2, 0),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT));
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, authored);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertWrittenOnTheEntering(authored, enteringPermanent);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Ally-nontoken-creature copy trigger records the entering permanent it copies (Necroduality)")
    void allyNontokenCreatureCreateTokenCopyRecordsEntering() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent());

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        assertThat(gd.stack.getFirst().lastKnownPermanentCard(enteringPermanent.getId())).isSameAs(entering);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isInstanceOf(CreateTokenCopyOfTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Opponent creature copy replacement trigger records the entering permanent and source")
    void opponentCreatureCopyReplacementRecordsEnteringAndSource() {
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        gd.playerBattlefields.put(opponentId, new ArrayList<>());

        Card source = new Card();
        source.setName("Faerie Artisans");
        addWatched(source, ZoneChangeWatch.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
                new ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect(
                        new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent()));
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(opponentId).add(enteringPermanent);

        enter(opponentId, entering);
        service.checkOpponentCreatureEntersTriggers(gd, opponentId, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(sourcePermanent.getId());
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isInstanceOf(ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Ally-token ability triggers when a creature token enters")
    void allyTokenTriggersForCreatureToken() {
        Card source = new Card();
        source.setName("Staff");
        addWatched(source, ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.STORY));
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card creatureToken = enteringCreature(1, 1);
        creatureToken.setToken(true);
        Permanent tokenPermanent = new Permanent(creatureToken);
        gd.playerBattlefields.get(player1Id).add(tokenPermanent);
        when(gameQueryService.findPermanentById(gd, tokenPermanent.getId())).thenReturn(tokenPermanent);

        enterTokens(player1Id, List.of(tokenPermanent.getId()));
        service.checkAllyTokenEntersTriggers(gd, player1Id, List.of(tokenPermanent.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isInstanceOf(PutCountersOnSelfEffect.class);
    }

    @Test
    @DisplayName("Any-creature scan queues a non-targeting trigger (Midnight Guard's untap)")
    void anyCreatureQueuesNonTargeting() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new UntapPermanentsEffect(TapUntapScope.SELF));

        enter(player1Id, enteringCreature(2, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(UntapPermanentsEffect.class);
    }

    @Test
    @DisplayName("Any-creature sacrifice-then trigger records the entering permanent")
    void anyCreatureSacrificeThenRecordsEnteringPermanent() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new SacrificeSelfThenEffect(new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)));
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);
        when(gameQueryService.findPermanentById(gd, enteringPermanent.getId())).thenReturn(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getTriggeringCardId()).isEqualTo(entering.getId());
    }

    @Test
    @DisplayName("Any-creature scan queues a generic targeting trigger for target selection")
    void anyCreatureQueuesTargeting() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new DestroyTargetPermanentEffect());

        enter(player1Id, enteringCreature(2, 2));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)).isTrue();
    }

    @Test
    @DisplayName("Ally-creature scan queues a graveyard target choice for optional exile, with the entering creature")
    void allyCreatureQueuesGraveyardTargeting() {
        var effect = ExileGraveyardCardsEffect.upToOneTargetFromOpponentGraveyard();
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, effect);
        Permanent source = gd.playerBattlefields.get(player1Id).getFirst();
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.SpellGraveyardTargetTrigger choice =
                gd.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
        assertThat(choice.effects()).containsExactly(effect);
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.sourcePermanentId()).isEqualTo(source.getId());
        assertThat(choice.triggeringPermanentId()).isEqualTo(enteringPermanent.getId());
    }

    @Test
    @DisplayName("A command-zone \"you may pay\" goes on the stack through the watch's default, recording the entering creature (Inalla)")
    void commandZoneMayPayRecordsTheEnteringCreature() {
        MayPayManaEffect authored = new MayPayManaEffect("{1}",
                new com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect(true, true)
                        .ofTriggeringPermanent(), "Pay {1}?");
        Card commander = new Card();
        commander.setName("Commander");
        addWatched(commander, ZoneChangeWatch.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, authored);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        TriggerCandidate candidate = new TriggerCandidate(commander, null, player1Id, Zone.COMMAND,
                commander.getTriggeredAbilities().getFirst());
        service.triggerZoneChange(gd, ZoneChangeWatch.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                candidate, List.of(authored), List.of(new ZoneChangeEvent(entering, player1Id, Zone.HAND,
                        Zone.BATTLEFIELD, ZoneChangeCause.OTHER, new Permanent(enteringPermanent), player1Id)));

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(authored);
            assertThat(entry.getCard()).isSameAs(commander);
            assertThat(entry.getSourcePermanentId()).isNull();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(enteringPermanent.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player1Id);
            assertThat(entry.isNonTargeting()).isTrue();
        });
    }

    @Test
    @DisplayName("Any-creature Soul Warden gain-life queues with no player target")
    void anyCreatureGainLife() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        enter(player1Id, enteringCreature(2, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    @Test
    void anyCreatureMayChoiceBelongsToSourceController() {
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        gd.playerBattlefields.put(opponentId, new ArrayList<>());
        MayEffect effect = new MayEffect(new GainLifeEffect(3), "Gain life?");
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, effect);
        Permanent source = gd.playerBattlefields.get(player1Id).getFirst();
        Card entering = enteringCreature(2, 2);
        gd.playerBattlefields.get(opponentId).add(new Permanent(entering));

        enter(opponentId, entering);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(source.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
        });
    }

    @Test
    void enteringControllerTargetedMayQueuesTargetBeforeTheOptionalChoice() {
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        gd.playerBattlefields.put(opponentId, new ArrayList<>());
        MayEffect effect = new MayEffect(
                com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(
                        new com.github.laxika.magicalvibes.model.amount.SourcePower()),
                "Deal damage?", null, com.github.laxika.magicalvibes.model.MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER);
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, effect);
        Card entering = enteringCreature(3, 3);
        Permanent permanent = new Permanent(entering);
        gd.playerBattlefields.get(opponentId).add(permanent);

        enter(opponentId, entering);

        assertThat(gd.pendingMayAbilities).isEmpty();
        PermanentChoiceContext.EntersTriggerTarget choice =
                gd.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.enteringPermanentId()).isEqualTo(permanent.getId());
        // The watcher is the ability's source; the entering creature deals the damage as the ability resolves.
        assertThat(choice.sourcePermanentId()).isNotEqualTo(permanent.getId());
    }

    @Test
    @DisplayName("Any-creature life-loss trigger records the entering creature and its controller")
    void anyCreatureLifeLossRecordsEnteringController() {
        UUID player2Id = UUID.randomUUID();
        gd.orderedPlayerIds.add(player2Id);
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player2Id).add(enteringPermanent);

        enter(player2Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isEqualTo(new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getTriggeringPermanentControllerId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("Creature entering from a graveyard queues an optional equipment attachment as written")
    void creatureFromGraveyardQueuesEquipmentAttachment() {
        Card source = new Card();
        source.setName("Equipment");
        MayEffect authored = new MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),
                "Attach this Equipment to it?");
        addWatched(source, ZoneChangeWatch.ON_CREATURE_ENTERS_FROM_GRAVEYARD, authored);
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        enteringPermanent.setEnteredFromGraveyardOwnerId(player1Id);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertWrittenOnTheEntering(authored, enteringPermanent);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(sourcePermanent.getId());
    }

    @Test
    @DisplayName("A \"you may pay\" copy of the entering creature goes on the stack as written, the creature recorded")
    void allyCreatureMayPayCopyRecordsTheEnteringPermanent() {
        MayPayManaEffect authored = new MayPayManaEffect("{2}",
                new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent(), "Pay {2}?");
        Card source = new Card();
        source.setName("Copier");
        addWatched(source, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, authored);
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertWrittenOnTheEntering(authored, enteringPermanent);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(sourcePermanent.getId());
    }

    @Test
    @DisplayName("A \"you may pay\" ability whose payload targets queues its target choice with the watcher as source")
    void allyCreatureMayPayTargetedQueuesChoiceWithTheWatcherAsSource() {
        MayPayManaEffect authored = new MayPayManaEffect("{2}{R}",
                DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower()), "Pay {2}{R}?");
        Card source = new Card();
        source.setName("Electro");
        addWatched(source, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, authored);
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);
        Card entering = enteringCreature(3, 3);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).isEmpty();
        var pending = gd.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThat(pending.effects()).containsExactly(authored);
        assertThat(pending.sourcePermanentId()).isEqualTo(sourcePermanent.getId());
        assertThat(pending.enteringPermanentId()).isEqualTo(enteringPermanent.getId());
    }

    @Test
    @DisplayName("A targeted ability on a creature entering from a graveyard is the watcher's, queued with its target choice")
    void creatureFromGraveyardTargetedAbilityIsTheWatchers() {
        DealDamageToAnyTargetEffect authored = DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower());
        Card source = new Card();
        source.setName("Flayer");
        addWatched(source, ZoneChangeWatch.ON_CREATURE_ENTERS_FROM_GRAVEYARD, authored);
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        enteringPermanent.setEnteredFromGraveyardOwnerId(player1Id);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).isEmpty();
        var pending = gd.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThat(pending.effects()).containsExactly(authored);
        assertThat(pending.sourcePermanentId()).isEqualTo(sourcePermanent.getId());
        assertThat(pending.enteringPermanentId()).isEqualTo(enteringPermanent.getId());
    }

    @Test
    @DisplayName("Any creature entry queues an optional Aura attachment as written")
    void anyCreatureEntryQueuesOptionalAuraAttachment() {
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        gd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));

        Card source = new Card();
        source.setName("Aura");
        MayEffect authored = new MayEffect(AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent(),
                "Attach this Aura to that creature?");
        addWatched(source, ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, authored);
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(opponentId).add(enteringPermanent);

        enter(opponentId, entering);

        assertWrittenOnTheEntering(authored, enteringPermanent);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(sourcePermanent.getId());
    }

    @Test
    @DisplayName("Ally-artifact scan queues a target choice for an optional targeted effect")
    void allyArtifactMayTargetQueuesChoice() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET), "Tap target permanent?"));

        Card enteringArtifact = new Card();
        enteringArtifact.setName("Artifact");
        enteringArtifact.setType(CardType.ARTIFACT);
        Permanent enteringPermanent = new Permanent(enteringArtifact);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);
        when(gameQueryService.findPermanentById(gd, enteringPermanent.getId())).thenReturn(enteringPermanent);
        when(gameQueryService.isArtifact(gd, enteringPermanent)).thenReturn(true);
        enter(player1Id, enteringArtifact);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)).isTrue();
    }

    @Test
    @DisplayName("Ally-equipment scan queues a may ability")
    void allyEquipmentQueuesMayAbility() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));

        Card enteringEquipment = new Card();
        enteringEquipment.setName("Equipment");
        enteringEquipment.setType(CardType.ARTIFACT);
        enteringEquipment.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        gd.playerBattlefields.get(player1Id).add(new Permanent(enteringEquipment));

        enter(player1Id, enteringEquipment);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(MayEffect.class);
    }

    @Test
    @DisplayName("Graveyard artifact scan queues a may ability")
    void graveyardArtifactQueuesMayAbility() {
        Card source = new Card();
        source.setName("Ovalchase Daredevil");
        addWatched(source, ZoneChangeWatch.GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        gd.playerGraveyards.put(player1Id,
                Collections.synchronizedList(new ArrayList<>(List.of(source))));

        Card enteringArtifact = new Card();
        enteringArtifact.setName("Entering Artifact");
        enteringArtifact.setType(CardType.ARTIFACT);
        Permanent enteringPermanent = new Permanent(enteringArtifact);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);
        when(gameQueryService.findPermanentById(gd, enteringPermanent.getId())).thenReturn(enteringPermanent);
        when(gameQueryService.isArtifact(gd, enteringPermanent)).thenReturn(true);

        enter(player1Id, enteringArtifact);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(MayEffect.class);
    }

    @Test
    @DisplayName("Any-permanent ability can see an artifact entering under an opponent's control")
    void anyPermanentAbilitySeesAnyController() {
        UUID player2Id = UUID.randomUUID();
        gd.orderedPlayerIds.add(player2Id);
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));

        Card source = new Card();
        source.setName("Arcbound Crusher");
        addWatched(source, ZoneChangeWatch.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));

        Card entering = new Card();
        entering.setName("Opponent Artifact");
        entering.setType(CardType.ARTIFACT);
        gd.playerBattlefields.get(player2Id).add(new Permanent(entering));

        enter(player2Id, entering);
        service.checkAnyPermanentEntersTriggers(gd, player2Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTriggeringPermanentControllerId()).isEqualTo(player2Id);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isInstanceOf(PutCountersOnSelfEffect.class);
    }

    @Test
    @DisplayName("Any-permanent targeted May effects queue enter-trigger target selection")
    void anyPermanentMayEffectQueuesTargetSelection() {
        gd.playerIds.add(player1Id);
        gd.playerIdToName.put(player1Id, "Player1");

        Card source = new Card();
        source.setName("Iceberg Cancrix");
        addWatched(source, ZoneChangeWatch.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new MayEffect(new MillEffect(2, MillRecipient.TARGET_PLAYER), "have target player mill two cards"));
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));

        Card entering = new Card();
        entering.setName("Snow-Covered Island");
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAnyPermanentEntersTriggers(gd, player1Id, entering);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)).isTrue();
        assertThat(gd.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class).effects().getFirst())
                .isInstanceOf(MayEffect.class);
    }

    @Test
    @DisplayName("Graft's \"you may move a counter onto that creature\" goes on the stack as written, no target, the entering creature its triggering permanent")
    void anyCreatureGraftGoesOnTheStackAsWritten() {
        MayEffect authored = new MayEffect(new MoveCounterFromSourceToEnteringCreatureEffect(
                CounterType.PLUS_ONE_PLUS_ONE), "Move a +1/+1 counter onto that creature?");
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, authored);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);
        when(gameQueryService.findPermanentById(gd, enteringPermanent.getId())).thenReturn(enteringPermanent);

        enter(player1Id, entering);

        assertWrittenOnTheEntering(authored, enteringPermanent);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("\"This deals 2 damage to it\" goes on the stack as written, no target, the entering creature its triggering permanent (Aether Flash)")
    void anyCreatureDamageToItGoesOnTheStackAsWritten() {
        DealDamageToTriggeringPermanentEffect authored = new DealDamageToTriggeringPermanentEffect(2);
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, authored);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);
        when(gameQueryService.findPermanentById(gd, enteringPermanent.getId())).thenReturn(enteringPermanent);

        enter(player1Id, entering);

        assertWrittenOnTheEntering(authored, enteringPermanent);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    @Test
    @DisplayName("Soulbond's \"you may pair that creature with this creature\" goes on the stack as written, no target, the entering creature its triggering permanent")
    void allyCreatureSoulbondGoesOnTheStackAsWritten() {
        MayEffect authored = new MayEffect(new SoulbondPairWithEnteringEffect(), "Pair Source with that creature?");
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, authored);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);
        when(gameQueryService.findPermanentById(gd, enteringPermanent.getId())).thenReturn(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertWrittenOnTheEntering(authored, enteringPermanent);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    @Test
    @DisplayName("\"It endures\" goes on the stack as written, no target, the entering creature its triggering permanent (Warden of the Grove)")
    void allyNontokenCreatureEndureGoesOnTheStackAsWritten() {
        EndureEffect authored = EndureEffect.forTriggeringPermanent(new Fixed(2));
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, authored);
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);
        when(gameQueryService.findPermanentById(gd, enteringPermanent.getId())).thenReturn(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertWrittenOnTheEntering(authored, enteringPermanent);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    /**
     * The one stack entry holds {@code authored} as the card wrote it, non-targeting, with {@code entering} as its
     * triggering permanent ("it", "that creature").
     */
    private void assertWrittenOnTheEntering(CardEffect authored, Permanent entering) {
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(authored);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(entering.getId());
        });
    }

    @Test
    @DisplayName("An emblem's enters ability goes on the stack as written with the entering permanent as \"it\"")
    void emblemEntersAbilityGoesOnTheStackWithTheEnteringPermanent() {
        MayEffect draw = new MayEffect(new DrawCardEffect(1), "Draw a card?");
        Card source = emblemSource(new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                draw));
        Card landCard = new Card();
        landCard.setName("Land");
        landCard.setType(CardType.LAND);
        Permanent land = new Permanent(landCard);
        gd.playerBattlefields.get(player1Id).add(land);
        when(predicateEvaluationService.matchesPermanentPredicate(eq(land), eq(Triggers.LAND),
                any(FilterContext.class))).thenReturn(true);

        service.checkEmblemEntersTriggers(gd, player1Id, landCard);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isSameAs(source);
            assertThat(entry.getDescription()).isEqualTo("Planeswalker's emblem");
            assertThat(entry.getEffectsToResolve()).containsExactly(draw);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(land.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player1Id);
        });
    }

    @Test
    @DisplayName("An emblem's \"you control\" enters ability ignores a permanent an opponent controls, and its filter")
    void emblemEntersAbilityIgnoresAnOpponentsPermanentAndANonMatchingOne() {
        emblemSource(new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new DrawCardEffect(1)));
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        Card opponentLandCard = new Card();
        opponentLandCard.setName("Opponent's land");
        opponentLandCard.setType(CardType.LAND);
        Permanent opponentLand = new Permanent(opponentLandCard);
        gd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>(List.of(opponentLand))));
        Card creatureCard = enteringCreature(2, 2);
        gd.playerBattlefields.get(player1Id).add(new Permanent(creatureCard));
        when(predicateEvaluationService.matchesPermanentPredicate(eq(opponentLand), eq(Triggers.LAND),
                any(FilterContext.class))).thenReturn(true);

        service.checkEmblemEntersTriggers(gd, opponentId, opponentLandCard);
        service.checkEmblemEntersTriggers(gd, player1Id, creatureCard);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)).isFalse();
    }

    @Test
    @DisplayName("A targeted emblem enters ability queues its target choice carrying the entering permanent")
    void targetedEmblemEntersAbilityQueuesItsTargetChoice() {
        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(3);
        Card source = emblemSource(new TriggeredAbility(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL),
                damage));
        Card artifactCard = new Card();
        artifactCard.setName("Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifact = new Permanent(artifactCard);
        gd.playerBattlefields.get(player1Id).add(artifact);
        when(predicateEvaluationService.matchesPermanentPredicate(eq(artifact), eq(Triggers.ARTIFACT),
                any(FilterContext.class))).thenReturn(true);

        service.checkEmblemEntersTriggers(gd, player1Id, artifactCard);

        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.SpellTargetTriggerAnyTarget choice =
                gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
        assertThat(choice.sourceCard()).isSameAs(source);
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.effects()).containsExactly(damage);
        assertThat(choice.triggeringPermanentId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("An emblem's \"put into your graveyard from the battlefield\" ability records the card and its "
            + "last-known permanent, and ignores a permanent an opponent owns")
    void emblemPutIntoYourGraveyardFromBattlefieldAbilityRecordsTheCard() {
        RegisterDelayedReturnDyingCreatureUnderControlEffect returnIt =
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true);
        Card source = emblemSource(new TriggeredAbility(
                Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN), returnIt));
        Card artifactCard = new Card();
        artifactCard.setName("Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifact = new Permanent(artifactCard);
        when(predicateEvaluationService.matchesPermanentPredicate(eq(artifact), eq(Triggers.ARTIFACT), any()))
                .thenReturn(true);
        UUID opponentId = UUID.randomUUID();

        service.checkEmblemPutIntoGraveyardFromBattlefieldTriggers(gd, opponentId, opponentId, artifactCard, artifact);
        assertThat(gd.stack).isEmpty();

        service.checkEmblemPutIntoGraveyardFromBattlefieldTriggers(gd, player1Id, player1Id, artifactCard, artifact);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isSameAs(source);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getEffectsToResolve()).containsExactly(returnIt);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringCardId()).isEqualTo(artifactCard.getId());
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(artifact.getId());
        });
    }

    @Test
    @DisplayName("An emblem's targeted \"whenever a creature dies\" ability queues its target choice with its own "
            + "target filter, and the death reader skips a \"put into your graveyard\" ability")
    void targetedEmblemCreatureDeathAbilityQueuesItsTargetChoice() {
        GivePoisonCountersEffect poison = new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER);
        TargetFilter opponent = TargetFilters.permanentAnOpponentControls();
        Card source = emblemSource(new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), poison)
                .withTargetFilter(opponent));
        gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L, List.of(new TriggeredAbility(
                Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN),
                new DrawCardEffect(1)))));
        Card creatureCard = enteringCreature(2, 2);
        Permanent creature = new Permanent(creatureCard);
        when(predicateEvaluationService.matchesPermanentPredicate(eq(creature), eq(Triggers.CREATURE),
                any(FilterContext.class))).thenReturn(true);

        service.checkCreatureDeathRegisteredTriggers(gd, player1Id, creature, 2, 2);

        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.DeathTriggerTarget choice =
                gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
        assertThat(choice.dyingCard()).isSameAs(source);
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.effects()).containsExactly(poison);
        assertThat(choice.targetFilter()).isSameAs(opponent);
        assertThat(choice.triggeringPermanent().getId()).isEqualTo(creature.getId());
    }

    /** A planeswalker card whose emblem, controlled by player 1, has {@code ability}. */
    private Card emblemSource(TriggeredAbility ability) {
        Card source = new Card();
        source.setName("Planeswalker");
        gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L, List.of(ability)));
        return source;
    }
}
