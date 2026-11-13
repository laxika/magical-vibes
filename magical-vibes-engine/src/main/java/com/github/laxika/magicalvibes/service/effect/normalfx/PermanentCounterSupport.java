package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.ChapterTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.SagaChapterService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.CounterAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.CounterWatch;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerJudge;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.trigger.CounterTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared permanent-counter helpers used by every migrated counter effect handler and by
 * {@code MultiPermanentChoiceHandlerService} (async proliferate / counter-placement re-entry).
 *
 * <p>Extracted verbatim from the original {@code PermanentCounterResolutionService} monolith;
 * behavior (counter placement, +1/+1 vs -1/-1 annihilation, saga lore chapters) is identical.
 */
@Slf4j
@Component
public class PermanentCounterSupport {

    private final GameQueryService gameQueryService;
    /** Vanishing's "when the last time counter is removed from this permanent, sacrifice it" (CR 702.63a). */
    private static final TriggeredAbility VANISHING_SACRIFICE = new TriggeredAbility(
            new CounterTrigger(CounterTrigger.CounterEvent.COUNTERS_REMOVED,
                    com.github.laxika.magicalvibes.model.trigger.ObjectRelation.THIS),
            new com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect());

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private TriggerCollectionService triggerCollectionService;
    private SagaChapterService sagaChapterService;
    private TriggeredAbilityStackService triggeredAbilityStackService;

    @Autowired
    void setTriggerCollectionService(@Lazy TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    @Autowired
    void setSagaChapterService(@Lazy SagaChapterService sagaChapterService) {
        this.sagaChapterService = sagaChapterService;
    }

    @Autowired
    void setTriggeredAbilityStackService(@Lazy TriggeredAbilityStackService triggeredAbilityStackService) {
        this.triggeredAbilityStackService = triggeredAbilityStackService;
    }

    public void notifyCountersPlaced(GameData gameData, StackEntry entry, Permanent target, int amount) {
        if (target == null || amount <= 0) {
            return;
        }
        UUID placingPlayerId = placingPlayerId(gameData, entry, target);
        if (triggerCollectionService != null) {
            triggerCollectionService.checkYouPutCountersTriggers(gameData, placingPlayerId, amount);
        }
        fireYouPutCountersOnCreatureTriggers(gameData, target, amount, placingPlayerId);
        fireCountersPutOnCreatureYouDontControlTriggers(gameData, target, amount, placingPlayerId);
    }

    public void notifyLastCounterRemoved(GameData gameData, Permanent source, CounterType counterType,
                                        int removed, UUID removingPlayerId) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (removed <= 0 || source.getCounterCount(counterType) != 0
                || !Objects.equals(controllerId, removingPlayerId)) {
            return;
        }
        CounterWatch watch = CounterWatch.ON_YOU_REMOVE_LAST_COUNTER_FROM_SELF;
        Permanent snapshot = new Permanent(source);
        for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
            CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
            requireNoQualifier(trigger, ability, watch);
            if (trigger.counterKind() != counterType) {
                continue;
            }
            putCounterAbility(gameData, ability, source, controllerId,
                    ConditionContext.forPermanent(source, controllerId), null, entry -> {
                        entry.setSourcePermanentSnapshot(snapshot);
                        entry.setEventCounterType(counterType);
                        entry.setEventValue(removed);
                    });
        }
    }

    /** Fires controller-scoped "whenever you put one or more counters on a creature" watchers. */
    private void fireYouPutCountersOnCreatureTriggers(
            GameData gameData, Permanent creature, int count, UUID placingPlayerId) {
        if (count <= 0 || creature == null || placingPlayerId == null
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            putCreatureCountersAbilities(gameData, source, placingPlayerId, CounterWatch.ON_YOU_PUT_COUNTERS_ON_CREATURE,
                    creature, creatureControllerId, count);
        }
    }

    /**
     * Hands each "whenever you put one or more counters on a creature [you don't control]" ability of {@code source} to
     * the stack door, recording the creature as its triggering permanent with its controller and the number of
     * counters as its event value, whatever its effects are.
     */
    private void putCreatureCountersAbilities(GameData gameData, Permanent source, UUID controllerId,
                                              CounterWatch watch, Permanent creature, UUID creatureControllerId,
                                              int count) {
        for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
            requireNothingToJudge(ability, watch);
            putCounterAbility(gameData, ability, source, controllerId,
                    ConditionContext.forPermanent(source, controllerId).withTriggeringPermanentId(creature.getId())
                            .withEventValue(count), null, entry -> {
                        entry.setNonTargeting(true);
                        entry.setEventValue(count);
                        entry.setTriggeringPermanentId(creature.getId());
                        entry.setTriggeringPermanentControllerId(creatureControllerId);
                    });
        }
    }

    /** Fires "whenever you put one or more counters on a creature you don't control" watchers. */
    public void fireCountersPutOnCreatureYouDontControlTriggers(
            GameData gameData, Permanent creature, int count, UUID placingPlayerId) {
        if (count <= 0 || creature == null || placingPlayerId == null
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (creatureControllerId == null || placingPlayerId.equals(creatureControllerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            putCreatureCountersAbilities(gameData, source, placingPlayerId,
                    CounterWatch.ON_YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL, creature, creatureControllerId,
                    count);
        }
    }

    public void notifyCountersPlaced(GameData gameData, StackEntry entry, Permanent target,
                                     CounterType counterType, int amount) {
        notifyCountersPlaced(gameData, entry, target, counterType, amount,
                placingPlayerId(gameData, entry, target));
    }

    private void notifyCountersPlaced(GameData gameData, StackEntry entry, Permanent target,
                                      CounterType counterType, int amount, UUID placingPlayerId) {
        if (target == null || amount <= 0) return;
        if (triggerCollectionService != null) {
            if (counterType == CounterType.LORE) {
                for (int i = 0; i < amount; i++) {
                    triggerCollectionService.checkYouPutLoreCounterOnSagaTriggers(
                            gameData, target, placingPlayerId);
                }
            }
            triggerCollectionService.checkYouPutCountersTriggers(gameData, placingPlayerId, amount);
            if (counterType == CounterType.TIME) {
                triggerCollectionService.checkYouPutTimeCountersTriggers(
                        gameData, target, placingPlayerId, amount);
            }
            fireYouPutCountersOnCreatureTriggers(gameData, target, amount, placingPlayerId);
        }
        fireCountersPutOnCreatureYouDontControlTriggers(gameData, target, amount, placingPlayerId);
        fireYouPutCountersOnAnotherCreatureTriggers(
                gameData, target, counterType, amount, placingPlayerId);
        fireOpponentPutsCountersOnControlledCreatureTriggers(
                gameData, target, counterType, amount, placingPlayerId);
    }

    public void notifyCountersPlaced(GameData gameData, StackEntry entry, Permanent target,
                                     int amount, CounterType counterType) {
        notifyCountersPlaced(gameData, entry, target, counterType, amount);
    }

    /** Fires Captain Marvel-style triggers once per counter-placement event. */
    public void fireYouPutCountersOnAnotherCreatureTriggers(
            GameData gameData, Permanent target, CounterType counterType, int amount,
            UUID placingPlayerId) {
        if (target == null || counterType == null || amount <= 0 || placingPlayerId == null
                || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }

        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.getId().equals(target.getId())) {
                continue;
            }
            // The intervening "if" ("if it's not a Kree") is judged against the creature the counters were put on as
            // the ability triggers (CR 603.4) and again as it resolves.
            for (TriggeredAbility ability : printedAbilities(gameData, source,
                    CounterWatch.ON_YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE)) {
                requireNothingToJudge(ability, CounterWatch.ON_YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE);
                putCounterAbility(gameData, ability, source, placingPlayerId,
                        ConditionContext.forPermanent(source, placingPlayerId)
                                .withTriggeringPermanentId(target.getId()).withEventValue(amount), null,
                        countersPutRecord(source, target, counterType, amount, placingPlayerId));
            }
        }
    }

    /**
     * The record of a "whenever [a player] puts one or more counters on a creature" ability of {@code source}, whatever
     * its effects are: the creature the counters were put on as its triggering permanent, the player who put them as
     * its triggering player, their kind as its event counter type and their number as its event value ("the same
     * number and kind of counters"), and the source as the ability triggered.
     */
    private static TriggerRecord countersPutRecord(Permanent source, Permanent target, CounterType counterType,
                                                   int amount, UUID placingPlayerId) {
        Permanent snapshot = new Permanent(source);
        return entry -> {
            entry.setTriggeringPermanentId(target.getId());
            entry.setTriggeringPlayerId(placingPlayerId);
            entry.setEventCounterType(counterType);
            entry.setEventValue(amount);
            entry.setNonTargeting(true);
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /** Fires "whenever an opponent puts counters on a creature they control" watchers. */
    public void fireOpponentPutsCountersOnControlledCreatureTriggers(
            GameData gameData, Permanent target, CounterType counterType, int amount,
            UUID placingPlayerId) {
        if (target == null || counterType == null || amount <= 0 || placingPlayerId == null
                || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null || !placingPlayerId.equals(targetControllerId)) {
            return;
        }

        CounterWatch watch = CounterWatch.ON_OPPONENT_PUT_COUNTERS_ON_CREATURE_THEY_CONTROL;
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            if (sourceControllerId.equals(placingPlayerId)) {
                return;
            }
            for (Permanent source : new ArrayList<>(battlefield)) {
                for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
                    requireNothingToJudge(ability, watch);
                    putCounterAbility(gameData, ability, source, sourceControllerId,
                            ConditionContext.forPermanent(source, sourceControllerId)
                                    .withTriggeringPermanentId(target.getId()).withEventValue(amount), null,
                            countersPutRecord(source, target, counterType, amount, placingPlayerId));
                }
            }
        });
    }

    public void notifySelfCountersPlaced(GameData gameData, StackEntry entry, Permanent target,
                                         CounterType counterType, int previousCount, int amount) {
        if (target != null && amount > 0) {
            boolean firstPlacementThisTurn = gameData.permanentsWithCountersPutThisTurn.add(target.getId());
            fireSelfCountersPutTriggers(gameData, target, counterType, previousCount, firstPlacementThisTurn);
            if (counterType == CounterType.LEVEL) {
                fireSelfReachesLevelTwoTriggers(gameData, target, previousCount);
                fireSelfReachesLevelThreeTriggers(gameData, target, previousCount);
            }
        }
    }

    public void firePlusOnePlusOneCountersPutOnOtherControlledHeroTriggers(
            GameData gameData, Permanent target, int count, UUID placingPlayerId) {
        if (target == null) {
            return;
        }
        firePlusOnePlusOneCountersPutOnOtherControlledHeroTriggers(
                gameData, List.of(target), count, placingPlayerId);
    }

    /**
     * Fires "whenever you put one or more +1/+1 counters on another Hero" once per watcher for a
     * single event that put counters on all of {@code targets} simultaneously.
     */
    public void firePlusOnePlusOneCountersPutOnOtherControlledHeroTriggers(
            GameData gameData, List<Permanent> targets, int count, UUID placingPlayerId) {
        if (count <= 0 || placingPlayerId == null) {
            return;
        }
        List<Permanent> heroTargets = targets.stream()
                .filter(target -> placingPlayerId.equals(
                        gameQueryService.findPermanentController(gameData, target.getId()))
                        && gameQueryService.isCreature(gameData, target)
                        && gameQueryService.effectiveCreatureSubtypes(gameData, target).contains(CardSubtype.HERO))
                .toList();
        if (heroTargets.isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }
        CounterWatch watch = CounterWatch.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_OTHER_HERO;
        for (Permanent source : new ArrayList<>(battlefield)) {
            if (heroTargets.stream().allMatch(target -> target.getId().equals(source.getId()))) {
                continue;
            }
            for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
                CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
                requireNoQualifier(trigger, ability, watch);
                requireWatchedEvent(trigger, ability, watch);
                putCounterAbility(gameData, ability, source, placingPlayerId,
                        ConditionContext.forPermanent(source, placingPlayerId).withEventValue(count), null,
                        entry -> entry.setEventValue(count));
            }
        }
    }

    public void firePlusOnePlusOneCountersPutOnAnotherCreatureTriggers(
            GameData gameData, Permanent target, int count, UUID placingPlayerId) {
        if (target == null || count <= 0 || placingPlayerId == null
                || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }
        CounterWatch watch = CounterWatch.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANOTHER_CREATURE;
        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.getId().equals(target.getId())) {
                continue;
            }
            for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
                CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
                requireWatchedEvent(trigger, ability, watch);
                // "Another colorless creature" (Omarthis): the trigger's filter on the creature.
                if (trigger.qualifier() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, target,
                        trigger.qualifier())) {
                    continue;
                }
                putCounterAbility(gameData, ability, source, placingPlayerId,
                        ConditionContext.forPermanent(source, placingPlayerId).withEventValue(count), null,
                        entry -> entry.setEventValue(count));
            }
        }
    }

    private final ConditionEvaluationService conditionEvaluationService;

    @Autowired
    public PermanentCounterSupport(GameQueryService gameQueryService,
                                   PredicateEvaluationService predicateEvaluationService,
                                   GameLogService gameLogService,
                                   PlayerInputService playerInputService,
                                   ConditionEvaluationService conditionEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    public PermanentCounterSupport(GameQueryService gameQueryService,
                                   PredicateEvaluationService predicateEvaluationService,
                                   GameLogService gameLogService,
                                   PlayerInputService playerInputService) {
        this(gameQueryService, predicateEvaluationService, gameLogService, playerInputService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService));
    }

    public void removeCountersAndTransform(GameData gameData, Permanent self, CounterType counterType, String counterName,
                                           boolean canTransform) {
        // Remove all counters of that type
        switch (counterType) {
            case CHARGE -> self.setCounterCount(CounterType.CHARGE, 0);
            case BLOODLINE -> self.setCounterCount(CounterType.BLOODLINE, 0);
            case HATCHLING -> self.setCounterCount(CounterType.HATCHLING, 0);
            case LANDMARK -> self.setCounterCount(CounterType.LANDMARK, 0);
            case SLIME -> self.setCounterCount(CounterType.SLIME, 0);
            case STUDY -> self.setCounterCount(CounterType.STUDY, 0);
            case RITUAL -> self.setCounterCount(CounterType.RITUAL, 0);
            case WISH -> self.setCounterCount(CounterType.WISH, 0);
            case BORE -> self.setCounterCount(CounterType.BORE, 0);
            case PLUS_ONE_PLUS_ONE -> self.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
            case MINUS_ONE_MINUS_ONE -> self.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
            default -> throw new IllegalStateException("Unsupported counter type: " + counterType);
        }

        gameLogService.append(gameData, GameLog.textCardText(
                "All " + counterName + " counters removed from ", self.getCard(), "."));
        log.info("Game {} - All {} counters removed from {}", gameData.id, counterName, self.getCard().getName());

        if (!canTransform) {
            return;
        }

        Card originalCard = self.getOriginalCard();
        if (!self.isTransformed()) {
            Card backFace = originalCard.getBackFaceCard();
            if (backFace != null) {
                Card frontCard = self.getCard();
                String frontName = frontCard.getName();
                self.setCard(backFace);
                self.setTransformed(true);
                gameLogService.append(gameData, GameLog.cardTextCard(frontCard, " transforms into ", backFace, "."));
                log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
            }
        } else {
            Card backCard = self.getCard();
            String backName = backCard.getName();
            self.setCard(originalCard);
            self.setTransformed(false);
            gameLogService.append(gameData, GameLog.cardTextCard(backCard, " transforms into ", originalCard, "."));
            log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
        }
    }

    public void applyPlusOnePlusOneCounters(GameData gameData, StackEntry entry, Permanent target, int counters) {
        if (counters <= 0 || gameQueryService.cantHavePlusOnePlusOneCounters(gameData, target)) {
            return;
        }
        applyPlusOnePlusOneCounters(gameData, entry, target, counters, placingPlayerId(gameData, entry, target));
    }

    /** As above, for an effect whose controller (the player putting the counters, CR 122.1) is already known. */
    public void applyPlusOnePlusOneCounters(GameData gameData, StackEntry entry, Permanent target, int counters,
                                            UUID placingPlayerId) {
        if (counters <= 0 || gameQueryService.cantHavePlusOnePlusOneCounters(gameData, target)) {
            return;
        }
        int previousCount = target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        counters = gameQueryService.replaceCounters(gameData, target, CounterType.PLUS_ONE_PLUS_ONE,
                counters, placingPlayerId);
        if (counters <= 0) {
            return;
        }
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
        notifyCountersPlaced(gameData, entry, target, counters, CounterType.PLUS_ONE_PLUS_ONE);
        notifySelfCountersPlaced(gameData, entry, target, CounterType.PLUS_ONE_PLUS_ONE, previousCount, counters);
        recordPlusOnePlusOneCounterPlacedOnCreature(gameData, target, placingPlayerId);
        recordCounterPlacedOnCreature(gameData, target, placingPlayerId);
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                gameData, target, counters, placingPlayerId);
        firePlusOnePlusOneCountersPutOnOtherControlledHeroTriggers(
                gameData, target, counters, placingPlayerId);
        firePlusOnePlusOneCountersPutOnAnotherCreatureTriggers(
                gameData, target, counters, placingPlayerId);

        String counterText = counters == 1 ? "a +1/+1 counter" : counters + " +1/+1 counters";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " gets " + counterText + "."));
        log.info("Game {} - {} gets {} +1/+1 counter(s)", gameData.id, target.getCard().getName(), counters);

        firePlusOnePlusOneCounterTriggers(gameData, target, placingPlayerId, List.of(), counters);
    }

    public void placeCountersOnPermanents(GameData gameData, StackEntry entry, List<UUID> permanentIds, CounterType counterType) {
        List<Card> affectedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !gameQueryService.cantHaveCounters(gameData, perm)) {
                UUID placingPlayerId = placingPlayerId(gameData, entry, perm);
                int previousCount = perm.getCounterCount(counterType);
                int placed = gameQueryService.replaceCounters(gameData, perm, counterType, 1,
                        placingPlayerId);
                if (placed <= 0) {
                    continue;
                }
                switch (counterType) {
                    case AIM -> perm.setCounterCount(CounterType.AIM, perm.getCounterCount(CounterType.AIM) + placed);
                    case CHARGE -> perm.setCounterCount(CounterType.CHARGE, perm.getCounterCount(CounterType.CHARGE) + placed);
                    case EGG -> perm.setCounterCount(CounterType.EGG, perm.getCounterCount(CounterType.EGG) + placed);
                    case HOUR -> perm.setCounterCount(CounterType.HOUR, perm.getCounterCount(CounterType.HOUR) + placed);
                    case HONE -> perm.setCounterCount(CounterType.HONE, perm.getCounterCount(CounterType.HONE) + placed);
                    case LEVEL -> perm.setCounterCount(CounterType.LEVEL, perm.getCounterCount(CounterType.LEVEL) + placed);
                    case RITUAL -> perm.setCounterCount(CounterType.RITUAL, perm.getCounterCount(CounterType.RITUAL) + placed);
                    case EVERYTHING -> perm.setCounterCount(CounterType.EVERYTHING,
                            perm.getCounterCount(CounterType.EVERYTHING) + placed);
                    case HASTE, DEATHTOUCH, DECAYED, FLYING, FIRST_STRIKE, DOUBLE_STRIKE, HEXPROOF,
                         INDESTRUCTIBLE, LIFELINK, REACH, TRAMPLE, MENACE, VIGILANCE, PRIMEVAL -> {
                        perm.setCounterCount(counterType, perm.getCounterCount(counterType) + placed);
                        perm.setCounterTimestamp(counterType, gameData.nextTimestamp());
                    }
                    default -> throw new IllegalArgumentException("Unsupported counter type for placement: " + counterType);
                }
                recordCounterPlacedOnCreature(gameData, perm, placingPlayerId);
                if (counterType == CounterType.PLUS_ONE_PLUS_ONE) {
                    recordPlusOnePlusOneCounterPlacedOnCreature(gameData, perm, placingPlayerId);
                }
                notifyCountersPlaced(gameData, entry, perm, placed, counterType);
                notifySelfCountersPlaced(gameData, entry, perm, counterType, previousCount, placed);
                fireCounterPutOnControlledCreatureTriggers(gameData, perm, placed, placingPlayerId);
                affectedCards.add(perm.getCard());
            }
        }

        if (!affectedCards.isEmpty()) {
            String counterName = counterType.name().toLowerCase();
            GameLog.Builder builder = GameLog.builder()
                    .card(entry.getCard())
                    .text(" puts an " + counterName + " counter on ");
            for (int i = 0; i < affectedCards.size(); i++) {
                if (i > 0) {
                    builder.text(", ");
                }
                builder.card(affectedCards.get(i));
            }
            builder.text(".");
            gameLogService.append(gameData, builder.build());
            log.info("Game {} - {} places {} counters on {} permanents", gameData.id,
                    entry.getCard().getName(), counterName, affectedCards.size());
        }
    }

    public void resolveCounterOnOwnPermanent(GameData gameData, StackEntry entry,
                                            CounterType counterType, int count, PermanentPredicate predicate) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withTriggeringPermanentId(entry.getTriggeringPermanentId());

        List<UUID> eligibleIds = new ArrayList<>();
        for (Permanent p : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(p, predicate, filterContext)
                    && !gameQueryService.cantBeChosen(gameData, p)) {
                eligibleIds.add(p.getId());
            }
        }

        if (eligibleIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": no eligible permanent to put counters on."));
            log.info("Game {} - {} no eligible permanent for counter placement", gameData.id, entry.getCard().getName());
            return;
        }

        if (eligibleIds.size() == 1) {
            Permanent target = gameQueryService.findPermanentById(gameData, eligibleIds.getFirst());
            if (target != null && !gameQueryService.cantHaveCounters(gameData, target)) {
                placeCounterOnPermanent(gameData, entry, target, counterType, count);
            }
        } else {
            // Multiple eligible — controller must choose one
            String counterName = counterTypeName(counterType);
            playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds,
                    1, new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(counterType, count, true),
                    "Choose a permanent to put " + count + " " + counterName + " counter(s) on.");
        }
    }

    public int placeCounterOnPermanent(GameData gameData, StackEntry entry, Permanent target,
                                       CounterType counterType, int count) {
        return placeCounterOnPermanent(gameData, entry, target, counterType, count, false, null);
    }

    public int placeCounterOnPermanent(GameData gameData, StackEntry entry, Permanent target,
                                       CounterType counterType, int count, boolean modularAbility) {
        return placeCounterOnPermanent(gameData, entry, target, counterType, count, modularAbility, null);
    }

    public int placeCounterOnPermanentForPlayer(GameData gameData, StackEntry entry, Permanent target,
                                                CounterType counterType, int count, UUID placingPlayerId) {
        return placeCounterOnPermanent(gameData, entry, target, counterType, count, false, placingPlayerId);
    }

    private int placeCounterOnPermanent(GameData gameData, StackEntry entry, Permanent target,
                                        CounterType counterType, int count, boolean modularAbility,
                                        UUID placingPlayerIdOverride) {
        return placeCounterOnPermanent(gameData, entry, target, counterType, count, modularAbility,
                placingPlayerIdOverride, false);
    }

    /** Applies the chosen replacement result without applying those replacements a second time. */
    public int placeCounterOnPermanentAfterReplacements(GameData gameData, StackEntry entry, Permanent target,
                                                        CounterType counterType, int count) {
        return placeCounterOnPermanent(gameData, entry, target, counterType, count, false, null, true);
    }

    private int placeCounterOnPermanent(GameData gameData, StackEntry entry, Permanent target,
                                        CounterType counterType, int count, boolean modularAbility,
                                        UUID placingPlayerIdOverride, boolean replacementsApplied) {
        if (gameQueryService.cantHaveCounters(gameData, target)) return 0;

        int previousLoreCount = counterType == CounterType.LORE
                ? target.getCounterCount(CounterType.LORE) : 0;
        int previousCount = target.getCounterCount(counterType);
        UUID counterPlacingPlayerId = placingPlayerIdOverride != null
                ? placingPlayerIdOverride : placingPlayerId(gameData, entry, target);
        count = replacementsApplied
                ? gameQueryService.limitCounters(gameData, target,
                gameQueryService.findPermanentController(gameData, target.getId()), counterType, count)
                : gameQueryService.replaceCounters(gameData, target, counterType, count,
                counterPlacingPlayerId, modularAbility);

        String counterName = switch (counterType) {
            case CHARGE -> { for (int i = 0; i < count; i++) target.setCounterCount(CounterType.CHARGE, target.getCounterCount(CounterType.CHARGE) + 1); yield "charge"; }
            case LEVEL -> { target.setCounterCount(CounterType.LEVEL, target.getCounterCount(CounterType.LEVEL) + count); yield "level"; }
            case LORE -> { for (int i = 0; i < count; i++) target.setCounterCount(CounterType.LORE, target.getCounterCount(CounterType.LORE) + 1); yield "lore"; }
            case LOYALTY -> { target.setCounterCount(CounterType.LOYALTY, target.getCounterCount(CounterType.LOYALTY) + count); yield "loyalty"; }
            case LUCK -> { target.setCounterCount(CounterType.LUCK, target.getCounterCount(CounterType.LUCK) + count); yield "luck"; }
            case OIL -> { target.setCounterCount(CounterType.OIL, target.getCounterCount(CounterType.OIL) + count); yield "oil"; }
            case PLUS_ONE_PLUS_ONE -> {
                if (count <= 0 || gameQueryService.cantHavePlusOnePlusOneCounters(gameData, target)) {
                    yield null;
                }
                target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + count);
                firePlusOnePlusOneCounterTriggers(gameData, target, counterPlacingPlayerId, List.of(), count);
                yield "+1/+1";
            }
            case PLUS_ONE_PLUS_ZERO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO) + count);
                yield "+1/+0";
            }
            case PLUS_TWO_PLUS_ZERO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PLUS_TWO_PLUS_ZERO, target.getCounterCount(CounterType.PLUS_TWO_PLUS_ZERO) + count);
                yield "+2/+0";
            }
            case PLUS_ONE_PLUS_TWO -> {
                if (count <= 0 || gameQueryService.cantHaveCounters(gameData, target)) { yield null; }
                target.setCounterCount(CounterType.PLUS_ONE_PLUS_TWO, target.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO) + count);
                yield "+1/+2";
            }
            case PLUS_TWO_PLUS_TWO -> {
                if (count <= 0 || gameQueryService.cantHaveCounters(gameData, target)) { yield null; }
                target.setCounterCount(CounterType.PLUS_TWO_PLUS_TWO, target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO) + count);
                yield "+2/+2";
            }
            case MINUS_ONE_MINUS_ONE -> {
                if (count <= 0 || gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) { yield null; }
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + count);
                yield "-1/-1";
            }
            case MINUS_ONE_MINUS_ZERO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ZERO, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ZERO) + count);
                yield "-1/-0";
            }
            case MINUS_TWO_MINUS_ONE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_TWO_MINUS_ONE, target.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE) + count);
                yield "-2/-1";
            }
            case MINUS_TWO_MINUS_TWO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_TWO_MINUS_TWO, target.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO) + count);
                yield "-2/-2";
            }
            case MINUS_ZERO_MINUS_ONE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_ZERO_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE) + count);
                yield "-0/-1";
            }
            case MINUS_ZERO_MINUS_TWO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_ZERO_MINUS_TWO, target.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO) + count);
                yield "-0/-2";
            }
            case PLUS_ZERO_PLUS_ONE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PLUS_ZERO_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE) + count);
                yield "+0/+1";
            }
            case PLUS_ZERO_PLUS_TWO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PLUS_ZERO_PLUS_TWO, target.getCounterCount(CounterType.PLUS_ZERO_PLUS_TWO) + count);
                yield "+0/+2";
            }
            case CARRION -> { target.setCounterCount(CounterType.CARRION, target.getCounterCount(CounterType.CARRION) + count); yield "carrion"; }
            case CUBE -> { target.setCounterCount(CounterType.CUBE, target.getCounterCount(CounterType.CUBE) + count); yield "cube"; }
            case CREDIT -> { target.setCounterCount(CounterType.CREDIT, target.getCounterCount(CounterType.CREDIT) + count); yield "credit"; }
            case CURRENCY -> { target.setCounterCount(CounterType.CURRENCY, target.getCounterCount(CounterType.CURRENCY) + count); yield "currency"; }
            case DOOM -> { target.setCounterCount(CounterType.DOOM, target.getCounterCount(CounterType.DOOM) + count); yield "doom"; }
            case DREAM -> { target.setCounterCount(CounterType.DREAM, target.getCounterCount(CounterType.DREAM) + count); yield "dream"; }
            case CORPSE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.CORPSE, target.getCounterCount(CounterType.CORPSE) + count);
                yield "corpse";
            }
            case DESPAIR -> { target.setCounterCount(CounterType.DESPAIR, target.getCounterCount(CounterType.DESPAIR) + count); yield "despair"; }
            case DEATH -> { target.setCounterCount(CounterType.DEATH, target.getCounterCount(CounterType.DEATH) + count); yield "death"; }
            case DELAY -> { target.setCounterCount(CounterType.DELAY, target.getCounterCount(CounterType.DELAY) + count); yield "delay"; }
            case EGG -> { target.setCounterCount(CounterType.EGG, target.getCounterCount(CounterType.EGG) + count); yield "egg"; }
            case DEVOTION -> { target.setCounterCount(CounterType.DEVOTION, target.getCounterCount(CounterType.DEVOTION) + count); yield "devotion"; }
            case DIVINITY -> { target.setCounterCount(CounterType.DIVINITY, target.getCounterCount(CounterType.DIVINITY) + count); yield "divinity"; }
            case HATCHLING -> { target.setCounterCount(CounterType.HATCHLING, target.getCounterCount(CounterType.HATCHLING) + count); yield "hatchling"; }
            case HOOFPRINT -> { target.setCounterCount(CounterType.HOOFPRINT, target.getCounterCount(CounterType.HOOFPRINT) + count); yield "hoofprint"; }
            case HUNGER -> { target.setCounterCount(CounterType.HUNGER, target.getCounterCount(CounterType.HUNGER) + count); yield "hunger"; }
            case INVITATION -> { target.setCounterCount(CounterType.INVITATION, target.getCounterCount(CounterType.INVITATION) + count); yield "invitation"; }
            case UNITY -> { target.setCounterCount(CounterType.UNITY, target.getCounterCount(CounterType.UNITY) + count); yield "unity"; }
            case KI -> { target.setCounterCount(CounterType.KI, target.getCounterCount(CounterType.KI) + count); yield "ki"; }
            case LANDMARK -> { target.setCounterCount(CounterType.LANDMARK, target.getCounterCount(CounterType.LANDMARK) + count); yield "landmark"; }
            case RITUAL -> { target.setCounterCount(CounterType.RITUAL, target.getCounterCount(CounterType.RITUAL) + count); yield "ritual"; }
            case STUDY -> { target.setCounterCount(CounterType.STUDY, target.getCounterCount(CounterType.STUDY) + count); yield "study"; }
            case WISH -> { target.setCounterCount(CounterType.WISH, target.getCounterCount(CounterType.WISH) + count); yield "wish"; }
            case SLEIGHT -> { target.setCounterCount(CounterType.SLEIGHT, target.getCounterCount(CounterType.SLEIGHT) + count); yield "sleight"; }
            case SLIME -> { target.setCounterCount(CounterType.SLIME, target.getCounterCount(CounterType.SLIME) + count); yield "slime"; }
            case STORAGE -> { target.setCounterCount(CounterType.STORAGE, target.getCounterCount(CounterType.STORAGE) + count); yield "storage"; }
            case AIM -> { target.setCounterCount(CounterType.AIM, target.getCounterCount(CounterType.AIM) + count); yield "aim"; }
            case ARROW -> { target.setCounterCount(CounterType.ARROW, target.getCounterCount(CounterType.ARROW) + count); yield "arrow"; }
            case BLAZE -> { target.setCounterCount(CounterType.BLAZE, target.getCounterCount(CounterType.BLAZE) + count); yield "blaze"; }
            case BLIGHT -> { target.setCounterCount(CounterType.BLIGHT, target.getCounterCount(CounterType.BLIGHT) + count); yield "blight"; }
            case BLOOD -> { target.setCounterCount(CounterType.BLOOD, target.getCounterCount(CounterType.BLOOD) + count); yield "blood"; }
            case BOUNTY -> { target.setCounterCount(CounterType.BOUNTY, target.getCounterCount(CounterType.BOUNTY) + count); yield "bounty"; }
            case BRIBERY -> { target.setCounterCount(CounterType.BRIBERY, target.getCounterCount(CounterType.BRIBERY) + count); yield "bribery"; }
            case BRICK -> { target.setCounterCount(CounterType.BRICK, target.getCounterCount(CounterType.BRICK) + count); yield "brick"; }
            case GEM -> { target.setCounterCount(CounterType.GEM, target.getCounterCount(CounterType.GEM) + count); yield "gem"; }
            case ELIXIR -> { target.setCounterCount(CounterType.ELIXIR, target.getCounterCount(CounterType.ELIXIR) + count); yield "elixir"; }
            case EON -> { target.setCounterCount(CounterType.EON, target.getCounterCount(CounterType.EON) + count); yield "eon"; }
            case EYEBALL -> { target.setCounterCount(CounterType.EYEBALL, target.getCounterCount(CounterType.EYEBALL) + count); yield "eyeball"; }
            case FADE -> { target.setCounterCount(CounterType.FADE, target.getCounterCount(CounterType.FADE) + count); yield "fade"; }
            case GOLD -> { target.setCounterCount(CounterType.GOLD, target.getCounterCount(CounterType.GOLD) + count); yield "gold"; }
            case GHOSTFORM -> { target.setCounterCount(CounterType.GHOSTFORM, target.getCounterCount(CounterType.GHOSTFORM) + count); yield "ghostform"; }
            case GROWTH -> { target.setCounterCount(CounterType.GROWTH, target.getCounterCount(CounterType.GROWTH) + count); yield "growth"; }
            case PRESSURE -> { target.setCounterCount(CounterType.PRESSURE, target.getCounterCount(CounterType.PRESSURE) + count); yield "pressure"; }
            case POLYP -> { target.setCounterCount(CounterType.POLYP, target.getCounterCount(CounterType.POLYP) + count); yield "polyp"; }
            case PLAGUE -> { target.setCounterCount(CounterType.PLAGUE, target.getCounterCount(CounterType.PLAGUE) + count); yield "plague"; }
            case QUEST -> { target.setCounterCount(CounterType.QUEST, target.getCounterCount(CounterType.QUEST) + count); yield "quest"; }
            case PUPA -> { target.setCounterCount(CounterType.PUPA, target.getCounterCount(CounterType.PUPA) + count); yield "pupa"; }
            case PAGE -> { target.setCounterCount(CounterType.PAGE, target.getCounterCount(CounterType.PAGE) + count); yield "page"; }
            case STUN -> { target.setCounterCount(CounterType.STUN, target.getCounterCount(CounterType.STUN) + count); yield "stun"; }
            case TOWER -> { target.setCounterCount(CounterType.TOWER, target.getCounterCount(CounterType.TOWER) + count); yield "tower"; }
            case TIME -> { target.setCounterCount(CounterType.TIME, target.getCounterCount(CounterType.TIME) + count); yield "time"; }
            case TREASURE -> { target.setCounterCount(CounterType.TREASURE, target.getCounterCount(CounterType.TREASURE) + count); yield "treasure"; }
            case AGE -> { target.setCounterCount(CounterType.AGE, target.getCounterCount(CounterType.AGE) + count); yield "age"; }
            case BAIT -> { target.setCounterCount(CounterType.BAIT, target.getCounterCount(CounterType.BAIT) + count); yield "bait"; }
            case VITALITY -> { target.setCounterCount(CounterType.VITALITY, target.getCounterCount(CounterType.VITALITY) + count); yield "vitality"; }
            case VALOR -> { target.setCounterCount(CounterType.VALOR, target.getCounterCount(CounterType.VALOR) + count); yield "valor"; }
            case HEALING -> { target.setCounterCount(CounterType.HEALING, target.getCounterCount(CounterType.HEALING) + count); yield "healing"; }
            case FEATHER -> { target.setCounterCount(CounterType.FEATHER, target.getCounterCount(CounterType.FEATHER) + count); yield "feather"; }
            case FATE -> { target.setCounterCount(CounterType.FATE, target.getCounterCount(CounterType.FATE) + count); yield "fate"; }
            case FILIBUSTER -> { target.setCounterCount(CounterType.FILIBUSTER, target.getCounterCount(CounterType.FILIBUSTER) + count); yield "filibuster"; }
            case FLOOD -> { target.setCounterCount(CounterType.FLOOD, target.getCounterCount(CounterType.FLOOD) + count); yield "flood"; }
            case HOURGLASS -> { target.setCounterCount(CounterType.HOURGLASS, target.getCounterCount(CounterType.HOURGLASS) + count); yield "hourglass"; }
            case PAIN -> { target.setCounterCount(CounterType.PAIN, target.getCounterCount(CounterType.PAIN) + count); yield "pain"; }
            case PARALYZATION -> { target.setCounterCount(CounterType.PARALYZATION, target.getCounterCount(CounterType.PARALYZATION) + count); yield "paralyzation"; }
            case PETAL -> { target.setCounterCount(CounterType.PETAL, target.getCounterCount(CounterType.PETAL) + count); yield "petal"; }
            case PETRIFICATION -> { target.setCounterCount(CounterType.PETRIFICATION, target.getCounterCount(CounterType.PETRIFICATION) + count); yield "petrification"; }
            case PIN -> { target.setCounterCount(CounterType.PIN, target.getCounterCount(CounterType.PIN) + count); yield "pin"; }
            case PREY -> { target.setCounterCount(CounterType.PREY, target.getCounterCount(CounterType.PREY) + count); yield "prey"; }
            case PRIMEVAL -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PRIMEVAL, target.getCounterCount(CounterType.PRIMEVAL) + count);
                target.setCounterTimestamp(CounterType.PRIMEVAL, gameData.nextTimestamp());
                yield "primeval";
            }
            case FUNGUS -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.FUNGUS, target.getCounterCount(CounterType.FUNGUS) + count);
                yield "fungus";
            }
            case SPORE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.SPORE, target.getCounterCount(CounterType.SPORE) + count);
                yield "spore";
            }
            case FUSE -> { target.setCounterCount(CounterType.FUSE, target.getCounterCount(CounterType.FUSE) + count); yield "fuse"; }
            case VERSE -> { target.setCounterCount(CounterType.VERSE, target.getCounterCount(CounterType.VERSE) + count); yield "verse"; }
            case ICE -> { target.setCounterCount(CounterType.ICE, target.getCounterCount(CounterType.ICE) + count); yield "ice"; }
            case INFECTION -> { target.setCounterCount(CounterType.INFECTION, target.getCounterCount(CounterType.INFECTION) + count); yield "infection"; }
            case INCARNATION -> { target.setCounterCount(CounterType.INCARNATION, target.getCounterCount(CounterType.INCARNATION) + count); yield "incarnation"; }
            case INCUBATION -> { target.setCounterCount(CounterType.INCUBATION, target.getCounterCount(CounterType.INCUBATION) + count); yield "incubation"; }
            case MAGNET -> { target.setCounterCount(CounterType.MAGNET, target.getCounterCount(CounterType.MAGNET) + count); yield "magnet"; }
            case MANIFESTATION -> { target.setCounterCount(CounterType.MANIFESTATION, target.getCounterCount(CounterType.MANIFESTATION) + count); yield "manifestation"; }
            case MINE -> { target.setCounterCount(CounterType.MINE, target.getCounterCount(CounterType.MINE) + count); yield "mine"; }
            case MUSIC -> { target.setCounterCount(CounterType.MUSIC, target.getCounterCount(CounterType.MUSIC) + count); yield "music"; }
            case MUSTER -> { target.setCounterCount(CounterType.MUSTER, target.getCounterCount(CounterType.MUSTER) + count); yield "muster"; }
            case NIGHT -> { target.setCounterCount(CounterType.NIGHT, target.getCounterCount(CounterType.NIGHT) + count); yield "night"; }
            case WIND -> { target.setCounterCount(CounterType.WIND, target.getCounterCount(CounterType.WIND) + count); yield "wind"; }
            case WINCH -> { target.setCounterCount(CounterType.WINCH, target.getCounterCount(CounterType.WINCH) + count); yield "winch"; }
            case WAGE -> { target.setCounterCount(CounterType.WAGE, target.getCounterCount(CounterType.WAGE) + count); yield "wage"; }
            case RUST -> { target.setCounterCount(CounterType.RUST, target.getCounterCount(CounterType.RUST) + count); yield "rust"; }
            case SOOT -> { target.setCounterCount(CounterType.SOOT, target.getCounterCount(CounterType.SOOT) + count); yield "soot"; }
            case SOUL -> { target.setCounterCount(CounterType.SOUL, target.getCounterCount(CounterType.SOUL) + count); yield "soul"; }
            case VORTEX -> { target.setCounterCount(CounterType.VORTEX, target.getCounterCount(CounterType.VORTEX) + count); yield "vortex"; }
            case VELOCITY -> { target.setCounterCount(CounterType.VELOCITY, target.getCounterCount(CounterType.VELOCITY) + count); yield "velocity"; }
            case TRAINING -> { target.setCounterCount(CounterType.TRAINING, target.getCounterCount(CounterType.TRAINING) + count); yield "training"; }
            case THEFT -> { target.setCounterCount(CounterType.THEFT, target.getCounterCount(CounterType.THEFT) + count); yield "theft"; }
            case TIDE -> { target.setCounterCount(CounterType.TIDE, target.getCounterCount(CounterType.TIDE) + count); yield "tide"; }
            case BASE_POWER_FOUR, BASE_TOUGHNESS_FOUR -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(counterType, target.getCounterCount(counterType) + count);
                target.setCounterTimestamp(counterType, gameData.nextTimestamp());
                yield counterType == CounterType.BASE_POWER_FOUR ? "base power 4" : "base toughness 4";
            }
            case HASTE, DEATHTOUCH, DECAYED, FLYING, FIRST_STRIKE, DOUBLE_STRIKE, HEXPROOF,
                 INDESTRUCTIBLE, LIFELINK, REACH, TRAMPLE, MENACE, VIGILANCE -> {
                target.setCounterCount(counterType, target.getCounterCount(counterType) + count);
                if (count > 0) {
                    target.setCounterTimestamp(counterType, gameData.nextTimestamp());
                }
                yield counterType.name().toLowerCase();
            }
            default -> {
                target.setCounterCount(counterType, target.getCounterCount(counterType) + count);
                yield counterType.name().toLowerCase();
            }
        };
        if (counterName == null || count <= 0) return 0;

        notifyCountersPlaced(gameData, entry, target, counterType, count, counterPlacingPlayerId);
        notifySelfCountersPlaced(gameData, entry, target, counterType, previousCount, count);
        recordCounterPlacedOnCreature(gameData, target, counterPlacingPlayerId);
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE) {
            recordPlusOnePlusOneCounterPlacedOnCreature(gameData, target, counterPlacingPlayerId);
            recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                    gameData, target, count, counterPlacingPlayerId);
            firePlusOnePlusOneCountersPutOnOtherControlledHeroTriggers(
                    gameData, target, count, counterPlacingPlayerId);
            firePlusOnePlusOneCountersPutOnAnotherCreatureTriggers(
                    gameData, target, count, counterPlacingPlayerId);
        }

        Card card = target.getCard();
        String counterText = count == 1 ? "a " + counterName + " counter" : count + " " + counterName + " counters";
        Card sourceCard = entry != null ? entry.getCard() : card;
        gameLogService.append(gameData, GameLog.cardTextCard(sourceCard, " puts " + counterText + " on ", card, "."));
        log.info("Game {} - {} puts {} {} counter(s) on {}", gameData.id,
                sourceCard.getName(), count, counterName, card.getName());

        // Lore counters on Sagas trigger each chapter crossed by the placement.
        if (counterType == CounterType.LOYALTY) {
            fireLoyaltyCountersPutOnPlaneswalkerTriggers(gameData, target, count);
        }

        // Lore counters on Sagas trigger chapter abilities (CR 715.2b)
        if (entry != null && counterType == CounterType.LORE && card.isSaga()) {
            int finalLoreCount = target.getCounterCount(CounterType.LORE);
            for (int loreCount = previousLoreCount + 1; loreCount <= finalLoreCount; loreCount++) {
                triggerSagaChapter(gameData, entry, target, loreCount);
            }
        }

        // Flourishing Defenses etc.: "whenever a -1/-1 counter is put on a creature." The placing player
        // is the resolving spell/ability's controller — read it from the entry rather than
        // currentlyResolvingControllerId, which is null when resolution was resumed asynchronously after a
        // target choice (e.g. Hapatra's combat-damage "put a -1/-1 counter on target creature").
        if (counterType == CounterType.MINUS_ONE_MINUS_ONE) {
            fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, target, count, counterPlacingPlayerId);
        } else if (counterType != CounterType.PLUS_ONE_PLUS_ONE) {
            fireCounterPutOnControlledCreatureTriggers(gameData, target, count, counterPlacingPlayerId);
        }
        return count;
    }

    public String counterTypeName(CounterType counterType) {
        return switch (counterType) {
            case CHARGE -> "charge";
            case LORE -> "lore";
            case LOYALTY -> "loyalty";
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case PLUS_ONE_PLUS_ZERO -> "+1/+0";
            case PLUS_TWO_PLUS_ZERO -> "+2/+0";
            case PLUS_ONE_PLUS_TWO -> "+1/+2";
            case PLUS_TWO_PLUS_TWO -> "+2/+2";
            case PLUS_ZERO_PLUS_TWO -> "+0/+2";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case MINUS_ONE_MINUS_ZERO -> "-1/-0";
            case MINUS_TWO_MINUS_TWO -> "-2/-2";
            case HATCHLING -> "hatchling";
            case STUDY -> "study";
            case WISH -> "wish";
            case SLIME -> "slime";
            case AIM -> "aim";
            case DELAY -> "delay";
            case BASE_POWER_FOUR -> "base power 4";
            case BASE_TOUGHNESS_FOUR -> "base toughness 4";
            case HEXPROOF_FROM_WHITE -> "hexproof from white";
            case HEXPROOF_FROM_BLUE -> "hexproof from blue";
            case HEXPROOF_FROM_BLACK -> "hexproof from black";
            case HEXPROOF_FROM_RED -> "hexproof from red";
            case HEXPROOF_FROM_GREEN -> "hexproof from green";
            default -> counterType.name().toLowerCase();
        };
    }

    public void removeCounterFromPermanent(GameData gameData, Permanent target,
                                            CounterType counterType, int amount) {
        int current = target.getCounterCount(counterType);
        if (current <= 0 || amount <= 0) {
            return;
        }

        int removed = Math.min(current, amount);
        target.setCounterCount(counterType, current - removed);
        checkLastTimeCounterRemoved(gameData, target, counterType, current);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(target, removed);
        }
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" removes " + removed + " " + counterTypeName(counterType) + " counter(s).")
                .build());
        log.info("Game {} - {} removes {} {} counter(s)", gameData.id, target.getCard().getName(),
                removed, counterTypeName(counterType));
    }

    public void removeCountersFromPermanent(GameData gameData, Permanent permanent,
                                            CounterType counterType, int count) {
        int remaining = count;
        if (counterType == CounterType.ANY) {
            int minusOneCounters = permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
            int removeMinusOne = Math.min(minusOneCounters, remaining);
            permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, minusOneCounters - removeMinusOne);
            remaining -= removeMinusOne;
            if (remaining > 0) {
                permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - remaining);
            }
        } else {
            int current = permanent.getCounterCount(counterType);
            int removed = Math.min(current, count);
            permanent.setCounterCount(counterType, current - removed);
            checkLastTimeCounterRemoved(gameData, permanent, counterType, current);
            if (counterType == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, removed);
            }
        }

        String counterName = counterTypeName(counterType);
        String counterText = count == 1
                ? "a " + counterName + " counter"
                : count + " " + counterName + " counters";
        gameLogService.append(gameData, GameLog.cardThen(
                permanent.getCard(), " removes " + counterText + "."));
    }

    /** Vanishing's sacrifice ability triggers whenever the last time counter is removed. */
    public void checkLastTimeCounterRemoved(GameData gameData, Permanent permanent,
                                              CounterType counterType, int previousCount) {
        if (counterType != CounterType.TIME || previousCount <= 0
                || permanent.getCounterCount(CounterType.TIME) != 0
                || !gameQueryService.hasKeyword(gameData, permanent,
                com.github.laxika.magicalvibes.model.Keyword.VANISHING)) return;
        UUID controllerId = controllerOf(gameData, permanent);
        if (controllerId == null) return;
        // The permanent's own ability: the effects that make its abilities trigger an additional time count it
        // (CR 603.3).
        Permanent snapshot = new Permanent(permanent);
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(VANISHING_SACRIFICE,
                permanent.getCard(), permanent, controllerId,
                permanent.getCard().getName() + "'s vanishing ability",
                ConditionContext.forPermanent(permanent, controllerId),
                entry -> entry.setSourcePermanentSnapshot(snapshot)));
    }

    /** Triggers {@code saga}'s chapter ability {@code loreCount}, reached by a lore counter {@code entry} put on it. */
    public void triggerSagaChapter(GameData gameData, StackEntry entry, Permanent saga, int loreCount) {
        sagaChapterService.triggerSagaChapter(gameData, saga, saga.getCard(), entry.getControllerId(), loreCount);
    }

    /**
     * Convenience overload that infers the placing player from
     * {@link GameData#currentlyResolvingControllerId} — correct for every counter placed while a spell
     * or ability resolves (target/mass/source counter effects). Combat callers that place counters
     * outside stack resolution (wither/infect damage, end-of-combat self counters) must use the
     * {@code placingPlayerId} overload so the controller-restricted watcher fires correctly.
     */
    public void fireMinusOneMinusOneCounterPutOnCreatureTriggers(GameData gameData, Permanent creature, int count) {
        fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, creature, count, gameData.currentlyResolvingControllerId);
    }

    public void fireMinusOneMinusOneCounterPutOnCreatureTriggers(GameData gameData, Permanent creature, int count, UUID placingPlayerId) {
        if (count <= 0 || creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        recordCounterPlacedOnCreature(gameData, creature, placingPlayerId);
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            boolean placedByThisController = controllerId.equals(placingPlayerId);
            for (Permanent source : new ArrayList<>(battlefield)) {
                // Per-counter watchers: global (Flourishing Defenses) + you-put (Nest of Scarabs), each ability one
                // object for each counter.
                List<TriggeredAbility> perCounterAbilities = new ArrayList<>(printedAbilities(gameData, source,
                        CounterWatch.ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE));
                if (placedByThisController) {
                    perCounterAbilities.addAll(printedAbilities(gameData, source,
                            CounterWatch.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE));
                }
                for (int i = 0; i < count; i++) {
                    for (TriggeredAbility ability : perCounterAbilities) {
                        putCounterAbility(gameData, ability, source, controllerId,
                                ConditionContext.forPermanent(source, controllerId)
                                        .withTriggeringPermanentId(creature.getId()).withEventValue(1), null,
                                countersPutRecord(source, creature, CounterType.MINUS_ONE_MINUS_ONE, 1,
                                        placingPlayerId));
                    }
                }

                // Global once-per-placement watcher (Auntie Ool): one trigger for this creature, even when the
                // placement put multiple -1/-1 counters on it. The affected creature is the ability's non-targeting
                // "that creature", with its controller at trigger time retained for zone changes.
                for (TriggeredAbility ability : printedAbilities(gameData, source,
                        CounterWatch.ON_MINUS_ONE_MINUS_ONE_COUNTERS_PUT_ON_CREATURE)) {
                    putCounterAbility(gameData, ability, source, controllerId,
                            ConditionContext.forPermanent(source, controllerId)
                                    .withTriggeringPermanentId(creature.getId()).withEventValue(count), null,
                            entry -> {
                                entry.setTargetId(creature.getId());
                                entry.setTriggeringPermanentId(creature.getId());
                                entry.setTriggeringPermanentControllerId(creatureControllerId);
                                entry.setNonTargeting(true);
                            });
                }

                // Once-per-creature you-put watcher (Hapatra, Nest of Scarabs): fires a single trigger
                // regardless of count; the count is carried as the event value for "that many" effects.
                if (placedByThisController) {
                    for (TriggeredAbility ability : printedAbilities(gameData, source,
                            CounterWatch.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE)) {
                        putCounterAbility(gameData, ability, source, controllerId,
                                ConditionContext.forPermanent(source, controllerId)
                                        .withTriggeringPermanentId(creature.getId()).withEventValue(count), null,
                                entry -> entry.setEventValue(count));
                    }
                }
            }
        });

        // Self-scoped "Whenever you put one or more -1/-1 counters on this creature" (Defiant Greatmaw):
        // fires once per event (not per counter), only when the controller is the placing player.
        fireSelfMinusOneMinusOneCountersPutTriggers(gameData, creature, placingPlayerId);
        fireCounterPutOnControlledCreatureTriggers(gameData, creature, count, placingPlayerId);
    }

    /**
     * Fires {@link CounterWatch#ON_SELF_MINUS_ONE_MINUS_ONE_COUNTERS_PUT} on {@code creature} — the -1/-1
     * mirror of {@link #firePlusOnePlusOneCountersPutOnSelfTriggers}. Fires once per placement event
     * (regardless of the counter count) and only when {@code placingPlayerId} is the creature's own
     * controller ("Whenever you put …"); a target is chosen as the ability goes on the stack (CR 603.3d).
     */
    void fireSelfMinusOneMinusOneCountersPutTriggers(GameData gameData, Permanent creature, UUID placingPlayerId) {
        UUID controllerId = controllerOf(gameData, creature);
        if (controllerId == null || !controllerId.equals(placingPlayerId)) {
            return;
        }
        for (TriggeredAbility ability : printedAbilities(gameData, creature,
                CounterWatch.ON_SELF_MINUS_ONE_MINUS_ONE_COUNTERS_PUT)) {
            putCounterAbility(gameData, ability, creature, controllerId,
                    ConditionContext.forPermanent(creature, controllerId), null, entry -> {
                    });
        }
    }

    private void fireSelfReachesLevelTwoTriggers(GameData gameData, Permanent permanent, int previousLevel) {
        if (previousLevel >= 1 || permanent.getCounterCount(CounterType.LEVEL) < 1) {
            return;
        }

        queueLevelReachedTriggers(gameData, permanent, CounterWatch.ON_SELF_REACHES_LEVEL_TWO,
                "reaches level 2");
    }

    /**
     * Class cards with separate level 2 and level 3 abilities use one level counter per
     * advancement, while the existing one-ability level cards use the historical three-counter
     * threshold for this trigger slot.
     */
    private void fireSelfReachesLevelThreeTriggers(GameData gameData, Permanent permanent, int previousLevel) {
        int levelThreeThreshold = permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD).stream()
                .filter(ActivatedAbility::isLevelUpAbility)
                .count() > 1 ? 2 : 3;
        if (previousLevel >= levelThreeThreshold
                || permanent.getCounterCount(CounterType.LEVEL) < levelThreeThreshold) {
            return;
        }

        queueLevelReachedTriggers(gameData, permanent, CounterWatch.ON_SELF_REACHES_LEVEL_THREE,
                "reaches level 3");
    }

    private void queueLevelReachedTriggers(GameData gameData, Permanent permanent, CounterWatch watch,
                                           String eventDescription) {
        UUID controllerId = controllerOf(gameData, permanent);
        if (controllerId == null) {
            return;
        }
        // Each "when this Class reaches level N" ability, whole (CR 603.3); a target or graveyard card is chosen as it
        // goes on the stack (CR 603.3d).
        for (TriggeredAbility ability : printedAbilities(gameData, permanent, watch)) {
            putCounterAbility(gameData, ability, permanent, controllerId,
                    ConditionContext.forPermanent(permanent, controllerId), null, entry -> {
                    });
            log.info("Game {} - {} {} trigger fires", gameData.id, permanent.getCard().getName(), eventDescription);
        }
    }

    /**
     * The printed abilities {@code watch} reads of {@code source}; none while it is face down (CR 708.2) or once it has
     * lost them.
     */
    private List<TriggeredAbility> printedAbilities(GameData gameData, Permanent source, CounterWatch watch) {
        if (source.isFaceDown() || source.isLosesAllAbilitiesUntilEndOfTurn()
                || gameQueryService.hasLostPrintedAbilities(gameData, source)) {
            return List.of();
        }
        return CounterAbilityReader.printed(source.getCard(), watch);
    }

    /**
     * Hands {@code ability}, a counter ability of {@code source} controlled by {@code controllerId}, to the stack door:
     * its event condition is judged now in {@code context} (CR 603.2) and the rest by {@link #putJudgedCounterAbility}.
     *
     * @return whether it triggered
     */
    private boolean putCounterAbility(GameData gameData, TriggeredAbility ability, Permanent source,
                                      UUID controllerId, ConditionContext context, UUID limitCreatureId,
                                      TriggerRecord record) {
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        return putJudgedCounterAbility(gameData, ability, source, controllerId, context, limitCreatureId, record);
    }

    /**
     * Hands {@code ability}, a counter ability of {@code source} controlled by {@code controllerId} whose event
     * condition was judged, to the stack door: the door judges its intervening "if" in {@code context} (CR 603.4) and
     * its limit ("once each turn for each creature" by {@code limitCreatureId}), counts the additional times it
     * triggers, stamps {@code record} on its entry whatever its effects are and chooses its targets as it goes on the
     * stack (CR 603.3d).
     *
     * @return whether it triggered
     */
    private boolean putJudgedCounterAbility(GameData gameData, TriggeredAbility ability, Permanent source,
                                            UUID controllerId, ConditionContext context, UUID limitCreatureId,
                                            TriggerRecord record) {
        Card card = source.getCard();
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card,
                source, controllerId, card.getName() + "'s triggered ability", context, record)
                .withLimitCreature(limitCreatureId));
        if (triggered) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
        }
        return triggered;
    }

    /**
     * Hands each printed ability of {@code source} that {@code watch} reads ("whenever this creature evolves", "when
     * this Class becomes level N") whole to the stack door, controlled by {@code controllerId}; a target or graveyard
     * card is chosen as it goes on the stack (CR 603.3d).
     */
    public void putOwnCounterAbilities(GameData gameData, Permanent source, UUID controllerId, CounterWatch watch) {
        for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
            putOwnCounterAbility(gameData, ability, source, controllerId);
        }
    }

    /** Hands {@code ability}, an ability of {@code source} about its own counters, whole to the stack door. */
    public boolean putOwnCounterAbility(GameData gameData, TriggeredAbility ability, Permanent source,
                                        UUID controllerId) {
        return putCounterAbility(gameData, ability, source, controllerId,
                ConditionContext.forPermanent(source, controllerId), null, entry -> {
                });
    }

    /** Whether only the ability's controller puts the counters ("you put one or more counters on …"). */
    private static boolean byController(CounterTrigger trigger) {
        return trigger.event() == CounterTrigger.CounterEvent.YOU_PUT_COUNTERS
                || trigger.event() == CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS;
    }

    /** Throws when {@code ability}'s trigger has a filter on the permanent, which the collector of {@code watch} doesn't judge. */
    private static void requireNoQualifier(CounterTrigger trigger, TriggeredAbility ability, CounterWatch watch) {
        if (trigger.qualifier() != null) {
            throw new IllegalStateException("The collector of " + watch + " judges no filter: " + ability);
        }
    }

    /** Throws when {@code ability}'s trigger is narrower than {@code watch}'s event, which its collector doesn't judge. */
    private static void requireWatchedEvent(CounterTrigger trigger, TriggeredAbility ability, CounterWatch watch) {
        if (trigger.event() != ((CounterTrigger) watch.trigger()).event()) {
            throw new IllegalStateException("The collector of " + watch + " reads no " + trigger.event() + ": "
                    + ability);
        }
    }

    /** Throws when {@code ability} has a filter, a narrower event or a limit, none of which {@code watch}'s collector judges. */
    private static void requireNothingToJudge(TriggeredAbility ability, CounterWatch watch) {
        CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
        requireNoQualifier(trigger, ability, watch);
        requireWatchedEvent(trigger, ability, watch);
        if (ability.options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The collector of " + watch + " judges no limit: " + ability);
        }
    }

    private UUID controllerOf(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(permanent)) {
                return playerId;
            }
        }
        return null;
    }

    public void firePlusOnePlusOneCounterTriggers(GameData gameData, Permanent target) {
        firePlusOnePlusOneCounterTriggers(gameData, target, placingPlayerId(gameData, null, target), List.of());
    }

    public void firePlusOnePlusOneCounterTriggers(GameData gameData, Permanent target,
                                                   UUID placingPlayerId) {
        firePlusOnePlusOneCounterTriggers(gameData, target, placingPlayerId, List.of());
    }

    public void firePlusOnePlusOneCounterTriggers(GameData gameData, Permanent target,
                                                   List<Permanent> excludedSources) {
        firePlusOnePlusOneCounterTriggers(gameData, target, placingPlayerId(gameData, null, target), excludedSources);
    }

    public void firePlusOnePlusOneCounterTriggers(GameData gameData, Permanent target,
                                                   UUID placingPlayerId, List<Permanent> excludedSources) {
        firePlusOnePlusOneCounterTriggers(gameData, target, placingPlayerId, excludedSources, 1);
    }

    public void firePlusOnePlusOneCounterTriggers(GameData gameData, Permanent target,
                                                   UUID placingPlayerId, List<Permanent> excludedSources, int count) {
        firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, target, placingPlayerId, count);
        // The number of counters put is the event's value; a caller that gives none means one placement.
        int placed = Math.max(1, count);
        firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                gameData, target, placed, placingPlayerId, excludedSources);
        firePlusOnePlusOneCountersPutOnControlledHumanTriggers(gameData, target, placingPlayerId, placed);
        fireCounterPutOnControlledCreatureTriggers(gameData, target, 1, placingPlayerId);
    }

    private void fireYouPutPlusOnePlusOneCountersOnAnyCreatureTriggers(
            GameData gameData, int count, UUID placingPlayerId) {
        if (count <= 0 || placingPlayerId == null) {
            return;
        }
        firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(gameData, placingPlayerId, count,
                CounterWatch.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANY_CREATURE);
    }

    /** Fires Human-scoped +1/+1-counter watchers for the controller of the affected permanent. */
    private void firePlusOnePlusOneCountersPutOnControlledHumanTriggers(
            GameData gameData, Permanent target, UUID placingPlayerId, int count) {
        if (target == null || !gameQueryService.isCreature(gameData, target)
                || !predicateEvaluationService.matchesPermanentPredicate(target,
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN), FilterContext.of(gameData))) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        CounterWatch watch = CounterWatch.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_HUMAN;
        for (Permanent source : new ArrayList<>(battlefield)) {
            for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
                CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
                requireNoQualifier(trigger, ability, watch);
                requireWatchedEvent(trigger, ability, watch);
                putCounterAbility(gameData, ability, source, controllerId,
                        ConditionContext.forPermanent(source, controllerId)
                                .withTriggeringPermanentId(target.getId()).withEventValue(count), null,
                        countersPutRecord(source, target, CounterType.PLUS_ONE_PLUS_ONE, count, placingPlayerId));
            }
        }
    }

    /** Fires generic counter-placement watchers on the creature's controller's battlefield. */
    public void fireCounterPutOnControlledCreatureTriggers(GameData gameData, Permanent creature, int count,
                                                            UUID placingPlayerId) {
        if (count <= 0 || creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        boolean firstCountersThisTurn = gameData.creaturesThatReceivedCountersThisTurn.add(creature.getId());
        CounterWatch watch = CounterWatch.ON_ALLY_COUNTER_PUT_ON_CREATURE;
        for (Permanent source : new ArrayList<>(battlefield)) {
            List<TriggeredAbility> abilities = printedAbilities(gameData, source, watch);
            for (int i = 0; i < count; i++) {
                for (TriggeredAbility ability : abilities) {
                    CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
                    // "You put" (Mikey & Leo, The Great Goblin) and the filter are the trigger's; the limit and the
                    // intervening "if" are the door's.
                    if (trigger.event() == CounterTrigger.CounterEvent.YOU_PUT_COUNTERS
                            && !Objects.equals(placingPlayerId, controllerId)) {
                        continue;
                    }
                    if (trigger.qualifier() != null && !predicateEvaluationService.matchesPermanentPredicate(
                            gameData, creature, trigger.qualifier())) {
                        continue;
                    }
                    // "If it's the first time counters have been put on that creature this turn" (Stalwart
                    // Successor), for each creature.
                    if (ability.options().limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE
                            && !firstCountersThisTurn) {
                        continue;
                    }
                    putCounterAbility(gameData, ability, source, controllerId,
                            ConditionContext.forPermanent(source, controllerId)
                                    .withTriggeringPermanentId(creature.getId()), creature.getId(), entry -> {
                                entry.setTriggeringPermanentId(creature.getId());
                                entry.setTriggeringPermanentControllerId(controllerId);
                            });
                }
            }
        }
    }

    /**
     * Fires controller-scoped triggers for loyalty counters placed on planeswalkers. The amount is
     * snapshotted on the triggered ability so "that many" remains the amount from the placement
     * event when the trigger resolves.
     */
    public void fireLoyaltyCountersPutOnControlledPlaneswalkersTriggers(
            GameData gameData, UUID controllerId, int count) {
        if (controllerId == null || count <= 0) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            queueLoyaltyCounterTrigger(gameData, source, controllerId, count);
        }
    }

    public void fireLoyaltyCountersPutOnSourceTrigger(GameData gameData, Permanent source, int count) {
        if (source == null || count <= 0) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        queueLoyaltyCounterTrigger(gameData, source, controllerId, count);
    }

    public void fireLoyaltyCountersPutOnPlaneswalkerTriggers(
            GameData gameData, Permanent planeswalker, int count) {
        if (planeswalker == null || count <= 0 || !gameQueryService.isPlaneswalker(gameData, planeswalker)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, planeswalker.getId());
        fireLoyaltyCountersPutOnControlledPlaneswalkersTriggers(gameData, controllerId, count);
    }

    private void queueLoyaltyCounterTrigger(GameData gameData, Permanent source,
                                            UUID controllerId, int count) {
        if (controllerId == null) {
            return;
        }
        CounterWatch watch = CounterWatch.ON_YOU_PUT_LOYALTY_COUNTERS_ON_PLANESWALKERS;
        for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
            requireNothingToJudge(ability, watch);
            putCounterAbility(gameData, ability, source, controllerId,
                    ConditionContext.forPermanent(source, controllerId).withEventValue(count), null,
                    entry -> entry.setEventValue(count));
        }
    }

    void firePlusOnePlusOneCountersPutOnSelfTriggers(GameData gameData, Permanent target) {
        firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, target, controllerOf(gameData, target));
    }

    private void firePlusOnePlusOneCountersPutOnSelfTriggers(GameData gameData, Permanent target,
                                                             UUID placingPlayerId) {
        firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, target, placingPlayerId, 1);
    }

    private void firePlusOnePlusOneCountersPutOnSelfTriggers(GameData gameData, Permanent target,
                                                             UUID placingPlayerId, int count) {
        CounterWatch watch = CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT;
        List<TriggeredAbility> abilities = printedAbilities(gameData, target, watch);
        if (abilities.isEmpty()) {
            return;
        }
        UUID controllerId = controllerOf(gameData, target);
        if (controllerId == null) {
            return;
        }

        for (TriggeredAbility ability : abilities) {
            CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
            requireNoQualifier(trigger, ability, watch);
            if (trigger.event() == CounterTrigger.CounterEvent.EACH_PLUS_ONE_COUNTER_PUT) {
                // "Whenever a +1/+1 counter is put on this" (Fathom Mage, Bloodcrazed Hoplite): once for each counter.
                for (int i = 0; i < count; i++) {
                    putCounterAbility(gameData, ability, target, controllerId,
                            ConditionContext.forPermanent(target, controllerId), null, entry -> {
                            });
                }
            } else if (!byController(trigger) || controllerId.equals(placingPlayerId)) {
                // "Whenever you put one or more +1/+1 counters on this" (Exemplar of Light, Blaster) only when its
                // controller puts them.
                putCounterAbility(gameData, ability, target, controllerId,
                        ConditionContext.forPermanent(target, controllerId), null, entry -> {
                        });
            }
        }
    }

    private void fireSelfCountersPutTriggers(GameData gameData, Permanent target,
                                              CounterType counterType, int previousCount,
                                              boolean firstPlacementThisTurn) {
        CounterWatch watch = CounterWatch.ON_SELF_COUNTERS_PUT;
        List<TriggeredAbility> abilities = new ArrayList<>(printedAbilities(gameData, target, watch));
        // A granted ability with a limit (Danny Pink's "draw a card", once each turn) triggers only on the turn's
        // first placement.
        gameQueryService.getGrantedEffects(gameData, target).stream()
                .filter(GrantTriggeredAbilityEffect.class::isInstance)
                .map(GrantTriggeredAbilityEffect.class::cast)
                .filter(grant -> grant.grantedAbility() instanceof TriggeredAbility ability
                        && ability.hasWatchedTrigger(watch.trigger())
                        && (firstPlacementThisTurn || ability.options().limit() == TriggerLimit.NONE))
                .map(grant -> (TriggeredAbility) grant.grantedAbility())
                .forEach(abilities::add);
        if (abilities.isEmpty()) {
            return;
        }

        UUID controllerId = controllerOf(gameData, target);
        if (controllerId == null) {
            return;
        }

        int currentCount = target.getCounterCount(counterType);
        for (TriggeredAbility ability : abilities) {
            CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
            requireNoQualifier(trigger, ability, watch);
            requireWatchedEvent(trigger, ability, watch);
            // "When the [Nth] counter is put on this" is the event (CR 603.2); an intervening "if" about the counters
            // is the state as they are put (CR 603.4), judged by the door.
            if (!counterTriggerConditionMet(gameData, ability.options().eventCondition(), target, controllerId,
                    counterType, previousCount, currentCount)) {
                continue;
            }
            putJudgedCounterAbility(gameData, ability, target, controllerId,
                    ConditionContext.forPermanent(target, controllerId), null, entry -> {
                    });
        }
    }

    /**
     * Fires the Wildwood Scourge-style watcher once for a +1/+1 counter-placement event on a
     * controlled non-Hydra creature other than the watcher.
     */
    private void firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
            GameData gameData, Permanent target) {
        firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                gameData, target, 1, null, List.of());
    }

    /** Fires watchers for a +1/+1 counter-placement event on another controlled creature. */
    private void firePlusOnePlusOneCountersPutOnAnotherCreatureTriggers(
            GameData gameData, Permanent target, int count, UUID placingPlayerId, List<Permanent> excludedSources) {
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        putAnotherCreatureCounterAbilities(gameData, target, count, placingPlayerId, excludedSources,
                CounterWatch.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_CREATURE);
    }

    /**
     * Hands each ability {@code watch} reads of the permanents of {@code target}'s controller other than {@code target}
     * and {@code excludedSources} to the stack door, recording the +1/+1 counters put on {@code target}
     * ({@link #countersPutRecord}).
     */
    private void putAnotherCreatureCounterAbilities(GameData gameData, Permanent target, int count,
                                                    UUID placingPlayerId, List<Permanent> excludedSources,
                                                    CounterWatch watch) {
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) {
            return;
        }
        Set<UUID> excludedSourceIds = excludedSources.stream().map(Permanent::getId)
                .collect(java.util.stream.Collectors.toSet());
        for (Permanent watcher : new ArrayList<>(battlefield)) {
            if (watcher.getId().equals(target.getId()) || excludedSourceIds.contains(watcher.getId())) {
                continue;
            }
            for (TriggeredAbility ability : printedAbilities(gameData, watcher, watch)) {
                requireNothingToJudge(ability, watch);
                putCounterAbility(gameData, ability, watcher, targetControllerId,
                        ConditionContext.forPermanent(watcher, targetControllerId)
                                .withTriggeringPermanentId(target.getId()).withEventValue(count), null,
                        countersPutRecord(watcher, target, CounterType.PLUS_ONE_PLUS_ONE, count, placingPlayerId));
            }
        }
    }

    public void firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
            GameData gameData, Permanent target, int count, UUID placingPlayerId) {
        firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                gameData, target, count, placingPlayerId, List.of());
    }

    public void firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
            GameData gameData, Permanent target, int count, UUID placingPlayerId,
            List<Permanent> excludedSources) {
        if (count > 0) {
            firePlusOnePlusOneCountersPutOnAnotherCreatureTriggers(gameData, target, count, placingPlayerId,
                    excludedSources);
        }
        if (count <= 0 || target == null || !gameQueryService.isCreature(gameData, target)
                || predicateEvaluationService.matchesPermanentPredicate(target,
                new PermanentHasSubtypePredicate(CardSubtype.HYDRA),
                FilterContext.of(gameData))) {
            return;
        }
        putAnotherCreatureCounterAbilities(gameData, target, count, placingPlayerId, excludedSources,
                CounterWatch.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_NON_HYDRA_CREATURE);
    }

    public void recordCounterPlacedOnCreature(GameData gameData, Permanent target, UUID placingPlayerId) {
        if (placingPlayerId == null || target == null) {
            return;
        }
        gameData.permanentsWithCountersPutByPlayerThisTurn
                .computeIfAbsent(placingPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(target.getId());
        if (gameQueryService.isCreature(gameData, target)) {
            gameData.playersWhoPutCountersOnCreaturesThisTurn.add(placingPlayerId);
        }
    }

    public void recordPlusOnePlusOneCountersPutOnControlledCreaturesThisTurn(
            GameData gameData, Permanent target, int count, UUID placingPlayerId) {
        if (target == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        recordPlusOnePlusOneCountersPutOnControlledCreaturesThisTurn(
                gameData, target, controllerId, count, placingPlayerId);
    }

    public void recordPlusOnePlusOneCountersPutOnControlledCreaturesThisTurn(
            GameData gameData, Permanent target, UUID controllerId, int count, UUID placingPlayerId) {
        if (count <= 0 || target == null || controllerId == null
                || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        if (triggerCollectionService != null) {
            triggerCollectionService.checkGraveyardAllyPlusOnePlusOneCountersPutOnCreatureTriggers(
                    gameData, target, controllerId, count);
        }
        if (controllerId.equals(placingPlayerId)) {
            gameData.plusOnePlusOneCountersPutOnControlledCreaturesThisTurn.merge(
                    controllerId, count, Integer::sum);
        }
    }

    /**
     * Records that {@code target} received +1/+1 counters this turn from {@code placingPlayerId}, for the
     * "put a +1/+1 counter on it this turn" checks. The placing handler owns any counter triggers.
     */
    public void recordPlusOnePlusOneCounterReceivedOnSource(GameData gameData, Permanent target,
                                                            UUID placingPlayerId) {
        if (target == null || placingPlayerId == null) {
            return;
        }
        gameData.permanentsWithPlusOneCountersPutByPlayerThisTurn
                .computeIfAbsent(placingPlayerId, ignored -> ConcurrentHashMap.newKeySet()).add(target.getId());
        gameData.permanentsThatReceivedPlusOnePlusOneCountersThisTurn.add(target.getId());
    }

    public void recordPlusOnePlusOneCounterPlacedOnCreature(GameData gameData, Permanent target,
                                                            UUID placingPlayerId) {
        if (placingPlayerId != null && target != null) {
            gameData.permanentsWithPlusOneCountersPutByPlayerThisTurn
                    .computeIfAbsent(placingPlayerId, ignored -> ConcurrentHashMap.newKeySet()).add(target.getId());
        }
        if (placingPlayerId != null && target != null && gameQueryService.isCreature(gameData, target)) {
            gameData.playersWhoPutPlusOnePlusOneCountersOnCreaturesThisTurn.add(placingPlayerId);
        }
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(GameData gameData, Permanent target) {
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, target, 1);
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(GameData gameData, Permanent target,
                                                                       int count) {
        if (target == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                gameData, target, controllerId, count,
                gameData.currentlyResolvingControllerId != null
                        ? gameData.currentlyResolvingControllerId : controllerId);
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
            GameData gameData, Permanent target, int count, UUID placingPlayerId) {
        if (target == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                gameData, target, controllerId, count, placingPlayerId);
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
            GameData gameData, Permanent target, UUID controllerId) {
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, target, controllerId, 1);
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
            GameData gameData, Permanent target, UUID controllerId, int count) {
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, target, controllerId, count, controllerId);
    }

    private void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
            GameData gameData, Permanent target, UUID controllerId, int count, UUID placingPlayerId) {
        if (target != null && count > 0 && placingPlayerId != null) {
            gameData.permanentsWithPlusOneCountersPutByPlayerThisTurn
                    .computeIfAbsent(placingPlayerId, ignored -> ConcurrentHashMap.newKeySet()).add(target.getId());
        }
        if (target != null && controllerId != null) {
            recordPlusOnePlusOneCountersPutOnControlledCreaturesThisTurn(
                    gameData, target, controllerId, count, placingPlayerId);
            boolean firstPlacementOnThisPermanent =
                    gameData.permanentsThatReceivedPlusOnePlusOneCountersThisTurn.add(target.getId());
            gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(controllerId);
            if (controllerId.equals(placingPlayerId)) {
                firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(gameData, controllerId, count);
            }
            if (gameQueryService.isCreature(gameData, target)) {
                if (controllerId.equals(placingPlayerId)) {
                    firePlusOnePlusOneCountersPutOnControlledCreatureTriggers(gameData, controllerId, count);
                }
                firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(gameData, controllerId, count,
                        CounterWatch.ON_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ALLY_CREATURE);
                fireYouPutPlusOnePlusOneCountersOnAnyCreatureTriggers(gameData, count, placingPlayerId);
            }
            if (firstPlacementOnThisPermanent) {
                fireFirstPlusOnePlusOneCounterPlacementOnAnotherPermanentTriggers(
                        gameData, target, controllerId, count, placingPlayerId);
            }
        }
    }

    private void fireFirstPlusOnePlusOneCounterPlacementOnAnotherPermanentTriggers(
            GameData gameData, Permanent target, UUID controllerId, int count, UUID placingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        CounterWatch watch = CounterWatch.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_PERMANENT_FIRST_TIME_EACH_TURN;
        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.getId().equals(target.getId())) {
                continue;
            }
            for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
                requireNothingToJudge(ability, watch);
                putCounterAbility(gameData, ability, source, controllerId,
                        ConditionContext.forPermanent(source, controllerId)
                                .withTriggeringPermanentId(target.getId()).withEventValue(count), null,
                        countersPutRecord(source, target, CounterType.PLUS_ONE_PLUS_ONE, count, placingPlayerId));
            }
        }
    }

    private void firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(GameData gameData,
                                                                              UUID controllerId, int count) {
        firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(gameData, controllerId, count,
                CounterWatch.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_PERMANENT);
    }

    private void firePlusOnePlusOneCountersPutOnControlledCreatureTriggers(GameData gameData,
                                                                            UUID controllerId, int count) {
        firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(gameData, controllerId, count,
                CounterWatch.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE);
    }

    private void firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(GameData gameData,
                                                                              UUID controllerId, int count,
                                                                              CounterWatch watch) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            for (TriggeredAbility ability : printedAbilities(gameData, source, watch)) {
                CounterTrigger trigger = CounterAbilityReader.trigger(ability, watch.trigger());
                requireNoQualifier(trigger, ability, watch);
                requireWatchedEvent(trigger, ability, watch);
                // The number of counters and the source as it triggered, whatever the effects are; the door marks
                // "do this only once each turn" as the action is taken (CR 603.2h).
                Permanent snapshot = new Permanent(source);
                putCounterAbility(gameData, ability, source, controllerId,
                        ConditionContext.forPermanent(source, controllerId).withEventValue(count), null, entry -> {
                            entry.setEventValue(count);
                            entry.setSourcePermanentSnapshot(snapshot);
                        });
            }
        }
    }

    private UUID placingPlayerId(GameData gameData, StackEntry entry, Permanent target) {
        if (entry != null && entry.getControllerId() != null) {
            return entry.getControllerId();
        }
        if (gameData.currentlyResolvingControllerId != null) {
            return gameData.currentlyResolvingControllerId;
        }
        return gameQueryService.findPermanentController(gameData, target.getId());
    }

    /**
     * Whether {@code condition}, an ability's event condition ({@code null} for none), holds as counters are put on
     * {@code target}: a lore- or other counter threshold ("when the [Nth] counter is put on this") holds only for the
     * counters that cross it, any other condition as it is now.
     */
    private boolean counterTriggerConditionMet(GameData gameData, Condition condition, Permanent target,
                                               UUID controllerId, CounterType counterType, int previousCount,
                                               int currentCount) {
        if (condition == null) {
            return true;
        }
        if (condition instanceof SourceCounterThreshold threshold) {
            return threshold.counterType() == counterType && previousCount < threshold.threshold()
                    && currentCount >= threshold.threshold();
        }
        return conditionEvaluationService.isMet(gameData, condition, ConditionContext.forPermanent(target, controllerId));
    }
}
