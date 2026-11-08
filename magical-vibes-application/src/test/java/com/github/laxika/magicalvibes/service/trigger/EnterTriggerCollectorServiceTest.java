package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
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
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
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
    @Mock private GraveyardTargetingService graveyardTargetingService;

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
        TriggerCollectorRegistry.scanBean(new EnterTriggerCollectorService(gameLogService,
                new AmountEvaluationService(predicateEvaluationService, gameQueryService), gameQueryService,
                graveyardTargetingService,
                predicateEvaluationService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService)), registry);

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
        addWatched(source, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new ChooseModeNotYetChosenThisTurnEffect(List.of(
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
    @DisplayName("Ally-creature may-transform-entering bakes the entering permanent (Vildin-Pack Alpha)")
    void allyCreatureMayTransformEntering() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TransformEnteringCreatureEffect());

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        MayEffect may = (MayEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
        assertThat(may.wrapped()).isInstanceOf(TransformTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Self-or-ally flying trigger materializes entering power for a may life gain")
    void selfOrAllyFlyingGainLifeUsesEnteringPower() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(new GainLifeEqualToPowerEffect(), "Gain life?"));

        Card entering = enteringCreature(4, 1);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        MayEffect may = (MayEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
        assertThat(may.wrapped()).isEqualTo(new GainLifeEffect(new TargetPower()));
    }

    @Test
    @DisplayName("Ally-creature may-put-counters queues on the entering creature (Mighty Emergence)")
    void allyCreaturePutCountersOnEntering() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnEnteringCreatureEffect(2));

        Card entering = enteringCreature(8, 8);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        MayEffect may = (MayEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
        assertThat(may.wrapped()).isInstanceOf(PutCounterOnTargetPermanentEffect.class);
    }

    @Test
    void nontokenCreatureCounterAndKeywordGrantShareOneNonTargetingAbility() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnEnteringCreatureEffect(1, false, List.of(
                        new GrantKeywordEffect(Keyword.HASTE,
                                com.github.laxika.magicalvibes.model.effect.GrantScope.TARGET))));
        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getEffectsToResolve()).hasSize(2);
    }

    @Test
    void nontokenCreatureCounterTriggerExcludesTokens() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnEnteringCreatureEffect(1, false));
        Card entering = enteringCreature(2, 2);
        entering.setToken(true);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        enter(player1Id, entering);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature oil-counter trigger puts the counter on the entering permanent")
    void allyCreatureCounterTriggerTargetsEnteringPermanent() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnEnteringCreatureEffect(CounterType.OIL, 1, false));

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        enteringPermanent.setCounterCount(CounterType.OIL, 1);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        PutCounterOnTargetPermanentEffect counter = (PutCounterOnTargetPermanentEffect)
                gd.stack.getFirst().getEffectsToResolve().getFirst();
        assertThat(counter.counterType()).isEqualTo(CounterType.OIL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
    }

    @Test
    @DisplayName("Ally-creature boost bakes a mandatory boost + haste grant on the entrant (Ogre Battledriver)")
    void allyCreatureBoostEntering() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new BoostEnteringCreatureEffect(2, 0, java.util.Set.of(Keyword.HASTE)));

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getEffectsToResolve())
                .hasSize(2)
                .hasAtLeastOneElementOfType(BoostTargetCreatureEffect.class)
                .hasAtLeastOneElementOfType(GrantKeywordEffect.class);
    }

    @Test
    @DisplayName("Ally-nontoken-creature copy trigger bakes the entering permanent as target (Necroduality)")
    void allyNontokenCreatureCreateTokenCopyBakesEntering() {
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new CreateTokenCopyOfTargetPermanentEffect());

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isInstanceOf(CreateTokenCopyOfTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Opponent creature copy replacement trigger bakes the entering permanent and source")
    void opponentCreatureCopyReplacementBakesEnteringAndSource() {
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        gd.playerBattlefields.put(opponentId, new ArrayList<>());

        Card source = new Card();
        source.setName("Faerie Artisans");
        addWatched(source, ZoneChangeWatch.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD, new ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect());
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(opponentId).add(enteringPermanent);

        enter(opponentId, entering);
        service.checkOpponentCreatureEntersTriggers(gd, opponentId, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
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
    @DisplayName("Ally-creature scan queues a graveyard target choice for optional exile")
    void allyCreatureQueuesGraveyardTargeting() {
        var effect = ExileGraveyardCardsEffect.upToOneTargetFromOpponentGraveyard();
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, effect);

        enter(player1Id, enteringCreature(2, 2));
        service.checkAllyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2), 0);

        assertThat(gd.stack).isEmpty();
        verify(graveyardTargetingService).handleGraveyardCardsExileETBTargeting(
                eq(gd), eq(player1Id), any(Card.class), eq(List.of(effect)), eq(effect));
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
                com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect.fromEnteringPermanent(
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
        assertThat(choice.sourceIsEnteringPermanent()).isTrue();
    }

    @Test
    @DisplayName("Any-creature life-loss trigger targets the entering creature's controller")
    void anyCreatureLifeLossTargetsEnteringController() {
        UUID player2Id = UUID.randomUUID();
        gd.orderedPlayerIds.add(player2Id);
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        addAllyCreatureTrigger(ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));

        enter(player2Id, enteringCreature(2, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isEqualTo(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("Creature entering from a graveyard queues an optional equipment attachment")
    void creatureFromGraveyardQueuesEquipmentAttachment() {
        Card source = new Card();
        source.setName("Equipment");
        addWatched(source, ZoneChangeWatch.ON_CREATURE_ENTERS_FROM_GRAVEYARD, new AttachSourceEquipmentToEnteringCreatureEffect());
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        enteringPermanent.setEnteredFromGraveyardOwnerId(player1Id);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        enter(player1Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(MayEffect.class);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(sourcePermanent.getId());
    }

    @Test
    @DisplayName("Any creature entry queues an optional Aura attachment")
    void anyCreatureEntryQueuesOptionalAuraAttachment() {
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        gd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));

        Card source = new Card();
        source.setName("Aura");
        addWatched(source, ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new AttachSourceAuraToEnteringCreatureEffect());
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(opponentId).add(enteringPermanent);

        enter(opponentId, entering);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve().getFirst()).isInstanceOf(MayEffect.class);
            assertThat(entry.getTargetId()).isEqualTo(enteringPermanent.getId());
            assertThat(entry.getSourcePermanentId()).isEqualTo(sourcePermanent.getId());
        });
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
        service.checkAllyArtifactEntersTriggers(gd, player1Id, enteringArtifact);

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
        service.checkAllyArtifactEntersTriggers(gd, player1Id, enteringArtifact);

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
}
