package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.CounterTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Triggers the counter abilities of a {@link CounterChangeEvent} (CR 603.2): counters put on or removed from a
 * permanent (CR 122.1), "whenever you put one or more counters on …", the abilities of a keyword action about counters
 * (proliferate, renown, monstrosity, training, evolve, a Class gaining a level, a player getting {E}). The abilities
 * are read from {@link TriggerIndex} as the layers leave them and matched by their {@link CounterTrigger}: its filter
 * on the permanent, "you put" and "once for each counter" are judged here, the ability's event condition as it
 * triggers, its intervening "if" (CR 603.4) and limit (CR 603.2h; "once each turn for each creature" by the creature)
 * by the door. Each is handed whole to the stack door with the record of the event.
 */
@Slf4j
@Service
public class CounterTriggers {

    private static final Trigger YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER = Triggers.youPutCountersOnPermanentOrPlayer();
    private static final Trigger YOU_PUT_TIME_COUNTERS = Triggers.youPutTimeCountersOnPermanentYouControl();
    private static final Trigger YOU_PUT_LORE_COUNTER = Triggers.youPutLoreCounterOnSagaYouControl();
    private static final Trigger YOU_PUT_COUNTERS_ON_CREATURE = Triggers.youPutCountersOnCreature();
    private static final Trigger YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL =
            Triggers.youPutCountersOnCreatureYouDontControl();
    private static final Trigger YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE = Triggers.youPutCountersOnAnotherCreature();
    private static final Trigger OPPONENT_PUTS_COUNTERS_ON_THEIR_CREATURE =
            Triggers.opponentPutsCountersOnCreatureTheyControl();
    private static final Trigger PLUS_ONE_ON_OTHER_HERO = Triggers.youPutPlusOneCountersOnAnotherHeroYouControl();
    private static final Trigger YOU_PUT_PLUS_ONE_ON_ANOTHER_CREATURE = Triggers.youPutPlusOneCountersOnAnotherCreature();
    private static final Trigger PLUS_ONE_ON_HUMAN = Triggers.plusOneCountersPutOnHumanYouControl();
    private static final Trigger COUNTERS_ON_CREATURE_YOU_CONTROL = Triggers.countersPutOnCreatureYouControl();
    private static final Trigger LOYALTY_ON_PLANESWALKERS = Triggers.loyaltyCountersPutOnPlaneswalkersYouControl();
    private static final Trigger PLUS_ONE_ON_THIS = Triggers.plusOneCountersPutOnThis();
    private static final Trigger COUNTERS_ON_THIS = Triggers.countersPutOnThis();
    private static final Trigger PLUS_ONE_ON_ANOTHER_CREATURE_YOU_CONTROL =
            Triggers.plusOneCountersPutOnAnotherCreatureYouControl();
    private static final Trigger PLUS_ONE_ON_ANOTHER_NON_HYDRA =
            Triggers.plusOneCountersPutOnAnotherNonHydraCreatureYouControl();
    private static final Trigger PLUS_ONE_ON_ANOTHER_PERMANENT_FIRST_TIME =
            Triggers.plusOneCountersPutOnAnotherPermanentYouControlFirstTimeEachTurn();
    private static final Trigger YOU_PUT_PLUS_ONE_ON_PERMANENT = Triggers.youPutPlusOneCountersOnPermanentYouControl();
    private static final Trigger YOU_PUT_PLUS_ONE_ON_CREATURE_YOU_CONTROL =
            Triggers.youPutPlusOneCountersOnCreatureYouControl();
    private static final Trigger PLUS_ONE_ON_CREATURE_YOU_CONTROL = Triggers.plusOneCountersPutOnCreatureYouControl();
    private static final Trigger YOU_PUT_PLUS_ONE_ON_ANY_CREATURE = Triggers.youPutPlusOneCountersOnCreature();
    private static final Trigger MINUS_ONE_COUNTER_ON_CREATURE = Triggers.minusOneCounterPutOnCreature();
    private static final Trigger MINUS_ONE_COUNTERS_ON_CREATURE = Triggers.minusOneCountersPutOnCreature();
    private static final Trigger YOU_PUT_MINUS_ONE_COUNTER = Triggers.youPutMinusOneCounterOnCreature();
    private static final Trigger YOU_PUT_MINUS_ONE_COUNTERS = Triggers.youPutMinusOneCountersOnCreature();
    private static final Trigger YOU_PUT_MINUS_ONE_ON_THIS = Triggers.youPutMinusOneCountersOnThis();
    private static final Trigger REACHES_LEVEL_TWO = Triggers.reachesLevelTwo();
    private static final Trigger REACHES_LEVEL_THREE = Triggers.reachesLevelThree();
    private static final Trigger YOU_REMOVE_LAST_COUNTER =
            new CounterTrigger(CounterTrigger.CounterEvent.YOU_REMOVE_LAST_COUNTER, ObjectRelation.THIS);
    private static final Trigger COUNTERS_REMOVED_FROM_PERMANENT_YOU_CONTROL =
            Triggers.countersRemovedFromPermanentYouControl();
    private static final Trigger TIME_COUNTERS_REMOVED = Triggers.timeCountersRemovedFromThis();
    private static final Trigger PLUS_ONE_COUNTER_REMOVED = Triggers.plusOneCounterRemovedFromThis();
    private static final Trigger LOYALTY_COUNTERS_REMOVED = Triggers.loyaltyCountersRemovedFromThis();
    private static final Trigger TIME_COUNTER_REMOVED_IN_EXILE = Triggers.timeCounterRemovedFromThisInExile();
    private static final Trigger BECOMES_RENOWNED = Triggers.becomesRenowned();
    private static final Trigger CREATURE_BECOMES_RENOWNED = Triggers.creatureYouControlBecomesRenowned();
    private static final Trigger BECOMES_MONSTROUS = Triggers.becomesMonstrous();
    private static final Trigger TRAINS = Triggers.trains();
    private static final Trigger EVOLVES = Triggers.evolves();
    private static final Trigger GETS_ENERGY = Triggers.youGetEnergy();
    private static final Trigger PROLIFERATES = Triggers.youProliferate();

    private final EventAbilitySupport abilities;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;

    public CounterTriggers(EventAbilitySupport abilities, @Lazy GameQueryService gameQueryService,
                           @Lazy PredicateEvaluationService predicateEvaluationService,
                           @Lazy ConditionEvaluationService conditionEvaluationService,
                           @Lazy GameLogService gameLogService) {
        this.abilities = abilities;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, CounterChangeEvent event) {
        Permanent permanent = event.permanent();
        UUID playerId = event.playerId();
        int amount = event.amount();
        switch (event.kind()) {
            case PLACED -> placed(gameData, permanent, event.counterType(), amount, playerId);
            case YOU_PUT_COUNTERS -> youPutCounters(gameData, playerId, amount);
            case YOU_PUT_TIME_COUNTERS -> youPutTimeCounters(gameData, permanent, playerId, amount);
            case YOU_PUT_LORE_COUNTER -> youPutLoreCounter(gameData, permanent, playerId);
            case SELF_PLACED -> selfPlaced(gameData, permanent, event.counterType(), event.previousCount(),
                    event.first());
            case YOU_PUT_ON_ANOTHER_CREATURE -> youPutCountersOnAnotherCreature(gameData, permanent,
                    event.counterType(), amount, playerId);
            case OPPONENT_PUTS_ON_OWN_CREATURE -> opponentPutsCountersOnOwnCreature(gameData, permanent,
                    event.counterType(), amount, playerId);
            case PLUS_ONE_ON_OTHER_HERO -> plusOneOnOtherHero(gameData, event.permanents(), amount, playerId);
            case PLUS_ONE_ON_ANOTHER_CREATURE -> youPutPlusOneOnAnotherCreature(gameData, permanent, amount,
                    playerId);
            case PLUS_ONE_PLACED -> plusOnePlaced(gameData, permanent, playerId, event.excluded(), amount);
            case PLUS_ONE_ON_SELF -> plusOneOnSelf(gameData, permanent, playerId, 1);
            case PLUS_ONE_ON_ANOTHER_NON_HYDRA -> plusOneOnAnotherNonHydra(gameData, permanent, amount, playerId,
                    event.excluded());
            case PLUS_ONE_ON_CONTROLLED_PERMANENT -> plusOneOnControlledPermanent(gameData, permanent,
                    event.controllerId(), amount, playerId, event.first());
            case GRAVEYARD_PLUS_ONE_ON_CREATURE -> graveyardPlusOneOnCreature(gameData, permanent,
                    event.controllerId(), amount);
            case ON_CONTROLLED_CREATURE -> onControlledCreature(gameData, permanent, event.controllerId(), amount,
                    playerId, event.first());
            case MINUS_ONE_PLACED -> minusOnePlaced(gameData, permanent, amount, playerId);
            case LOYALTY_ON_PLANESWALKERS -> loyaltyOnPlaneswalkers(gameData, playerId, amount);
            case LAST_REMOVED -> lastRemoved(gameData, permanent, event.counterType(), amount, playerId);
            case REMOVED -> removed(gameData);
            case TIME_COUNTER_REMOVED_IN_EXILE -> timeCounterRemovedInExile(gameData, event.card(), playerId,
                    amount, event.controllerId());
            case RENOWNED -> renowned(gameData, permanent, playerId);
            case MONSTROUS -> monstrous(gameData, permanent, playerId, amount);
            case TRAINED -> trained(gameData, permanent, playerId);
            case EVOLVED -> putOwnAbilities(gameData, permanent, playerId, EVOLVES);
            case LEVEL_REACHED -> putOwnAbilities(gameData, permanent, playerId,
                    amount == 2 ? REACHES_LEVEL_TWO : REACHES_LEVEL_THREE);
            case ENERGY -> energy(gameData, playerId, amount);
            case PROLIFERATED -> proliferated(gameData, playerId, amount);
        }
    }

    /**
     * Counters were put on {@code target} by {@code placingPlayerId}: each lore counter's "whenever you put a lore
     * counter on a Saga you control", "whenever you put one or more counters on a permanent or player", the time
     * counters' "whenever you put one or more time counters", "whenever you put one or more counters on a creature
     * [you don't control]", "on another creature", and the opponents' "whenever an opponent puts counters on a
     * creature they control". {@code counterType} is {@code null} when the kind isn't known, which the kind-reading
     * abilities then don't see.
     */
    private void placed(GameData gameData, Permanent target, CounterType counterType, int amount,
                        UUID placingPlayerId) {
        if (target == null || amount <= 0) return;
        if (counterType == CounterType.LORE) {
            for (int i = 0; i < amount; i++) {
                youPutLoreCounter(gameData, target, placingPlayerId);
            }
        }
        youPutCounters(gameData, placingPlayerId, amount);
        if (counterType == CounterType.TIME) {
            youPutTimeCounters(gameData, target, placingPlayerId, amount);
        }
        youPutCountersOnCreature(gameData, target, amount, placingPlayerId, false);
        youPutCountersOnCreature(gameData, target, amount, placingPlayerId, true);
        youPutCountersOnAnotherCreature(gameData, target, counterType, amount, placingPlayerId);
        opponentPutsCountersOnOwnCreature(gameData, target, counterType, amount, placingPlayerId);
    }

    private void youPutCounters(GameData gameData, UUID placingPlayerId, int amount) {
        if (placingPlayerId == null || amount <= 0) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            // "Whenever you put one or more counters on a permanent or player, … deals that much damage to target …"
            // (All Will Be One): the placing player and the number of counters put ("that much").
            putUnjudgedAbilities(gameData, permanent, placingPlayerId, YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER,
                    ConditionContext.forPermanent(permanent, placingPlayerId).withEventValue(amount),
                    EventAbilitySupport.eventRecord(permanent, null, placingPlayerId, amount, null, null));
        }
    }

    /** One time-counter placement on {@code target}, a permanent {@code placingPlayerId} controls. */
    private void youPutTimeCounters(GameData gameData, Permanent target, UUID placingPlayerId, int amount) {
        if (target == null || placingPlayerId == null || amount <= 0
                || !placingPlayerId.equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            putUnjudgedAbilities(gameData, permanent, placingPlayerId, YOU_PUT_TIME_COUNTERS,
                    ConditionContext.forPermanent(permanent, placingPlayerId).withEventValue(amount),
                    EventAbilitySupport.eventRecord(permanent, null, placingPlayerId, amount, target,
                            placingPlayerId));
        }
    }

    /** One lore counter placed on {@code saga}, a Saga {@code placingPlayerId} controls. */
    private void youPutLoreCounter(GameData gameData, Permanent saga, UUID placingPlayerId) {
        if (saga == null || placingPlayerId == null || !saga.getCard().isSaga()
                || !placingPlayerId.equals(gameQueryService.findPermanentController(gameData, saga.getId()))) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            // "Whenever you put a lore counter on a Saga you control, put a +1/+1 counter on up to one other target
            // creature" (Sigurd, Jarl of Ravensthorpe): the Saga is the triggering permanent.
            putUnjudgedAbilities(gameData, permanent, placingPlayerId, YOU_PUT_LORE_COUNTER,
                    ConditionContext.forPermanent(permanent, placingPlayerId),
                    EventAbilitySupport.eventRecord(permanent, null, placingPlayerId, null, saga, placingPlayerId));
        }
    }

    /**
     * "Whenever you put one or more counters on a creature" and, when {@code youDontControl}, "… on a creature you
     * don't control": the creature is the triggering permanent with its controller, the number of counters the
     * event's value.
     */
    private void youPutCountersOnCreature(GameData gameData, Permanent creature, int count, UUID placingPlayerId,
                                          boolean youDontControl) {
        if (count <= 0 || creature == null || placingPlayerId == null
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (youDontControl && (creatureControllerId == null || placingPlayerId.equals(creatureControllerId))) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }
        Trigger watched = youDontControl ? YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL : YOU_PUT_COUNTERS_ON_CREATURE;
        for (Permanent source : new ArrayList<>(battlefield)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, source, watched)) {
                requireNothingToJudge(ability, watched);
                putPlacementAbility(gameData, ability, source, placingPlayerId,
                        ConditionContext.forPermanent(source, placingPlayerId)
                                .withTriggeringPermanentId(creature.getId()).withEventValue(count), null, entry -> {
                            entry.setNonTargeting(true);
                            entry.setEventValue(count);
                            entry.setTriggeringPermanentId(creature.getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                        });
            }
        }
    }

    /**
     * "Whenever you put one or more counters on another creature" (Captain Marvel): its intervening "if" ("if it's not
     * a Kree") is judged against the creature the counters were put on as the ability triggers (CR 603.4) and again
     * as it resolves.
     */
    private void youPutCountersOnAnotherCreature(GameData gameData, Permanent target, CounterType counterType,
                                                 int amount, UUID placingPlayerId) {
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
            for (TriggeredAbility ability : abilities.abilities(gameData, source,
                    YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE)) {
                requireNothingToJudge(ability, YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE);
                putPlacementAbility(gameData, ability, source, placingPlayerId,
                        ConditionContext.forPermanent(source, placingPlayerId)
                                .withTriggeringPermanentId(target.getId()).withEventValue(amount), null,
                        countersPutRecord(source, target, counterType, amount, placingPlayerId));
            }
        }
    }

    /** "Whenever an opponent puts counters on a creature they control". */
    private void opponentPutsCountersOnOwnCreature(GameData gameData, Permanent target, CounterType counterType,
                                                   int amount, UUID placingPlayerId) {
        if (target == null || counterType == null || amount <= 0 || placingPlayerId == null
                || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null || !placingPlayerId.equals(targetControllerId)) {
            return;
        }
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            if (sourceControllerId.equals(placingPlayerId)) {
                return;
            }
            for (Permanent source : new ArrayList<>(battlefield)) {
                for (TriggeredAbility ability : abilities.abilities(gameData, source,
                        OPPONENT_PUTS_COUNTERS_ON_THEIR_CREATURE)) {
                    requireNothingToJudge(ability, OPPONENT_PUTS_COUNTERS_ON_THEIR_CREATURE);
                    putPlacementAbility(gameData, ability, source, sourceControllerId,
                            ConditionContext.forPermanent(source, sourceControllerId)
                                    .withTriggeringPermanentId(target.getId()).withEventValue(amount), null,
                            countersPutRecord(source, target, counterType, amount, placingPlayerId));
                }
            }
        });
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

    /**
     * Counters were put on {@code target} itself: its own "whenever one or more counters are put on this" ("when the
     * [Nth] counter is put on this" for the counters that cross it), and a Class's "when this becomes level N".
     * A granted ability with a limit (Danny Pink's "draw a card", once each turn) triggers only on the turn's first
     * placement.
     */
    private void selfPlaced(GameData gameData, Permanent target, CounterType counterType, int previousCount,
                            boolean firstPlacementThisTurn) {
        if (target == null) {
            return;
        }
        selfCountersPut(gameData, target, counterType, previousCount, firstPlacementThisTurn);
        if (counterType == CounterType.LEVEL) {
            selfReachesLevelTwo(gameData, target, previousCount);
            selfReachesLevelThree(gameData, target, previousCount);
        }
    }

    private void selfCountersPut(GameData gameData, Permanent target, CounterType counterType, int previousCount,
                                 boolean firstPlacementThisTurn) {
        List<TriggeredAbility> own = target.getCard().getTriggeredAbilities();
        List<TriggeredAbility> found = abilities.abilities(gameData, target, COUNTERS_ON_THIS).stream()
                .filter(ability -> own.contains(ability) || firstPlacementThisTurn
                        || ability.options().limit() == TriggerLimit.NONE)
                .toList();
        if (found.isEmpty()) {
            return;
        }
        UUID controllerId = gameData.findControllerOf(target);
        if (controllerId == null) {
            return;
        }
        int currentCount = target.getCounterCount(counterType);
        for (TriggeredAbility ability : found) {
            CounterTrigger trigger = counterTrigger(ability, COUNTERS_ON_THIS);
            requireNoQualifier(trigger, ability, COUNTERS_ON_THIS);
            requireWatchedEvent(trigger, ability, COUNTERS_ON_THIS);
            // "When the [Nth] counter is put on this" is the event (CR 603.2); an intervening "if" about the counters
            // is the state as they are put (CR 603.4), judged by the door.
            if (!counterTriggerConditionMet(gameData, ability.options().eventCondition(), target, controllerId,
                    counterType, previousCount, currentCount)) {
                continue;
            }
            Card card = target.getCard();
            abilities.putJudged(gameData, ability, card, target, controllerId,
                    ConditionContext.forPermanent(target, controllerId), entry -> {
                    }, null, card.getName() + "'s triggered ability",
                    GameLog.cardThen(card, "'s triggered ability triggers."));
        }
    }

    private void selfReachesLevelTwo(GameData gameData, Permanent permanent, int previousLevel) {
        if (previousLevel >= 1 || permanent.getCounterCount(CounterType.LEVEL) < 1) {
            return;
        }
        levelReached(gameData, permanent, REACHES_LEVEL_TWO, "reaches level 2");
    }

    /**
     * Class cards with separate level 2 and level 3 abilities use one level counter per advancement, while the
     * one-ability level cards use the three-counter threshold.
     */
    private void selfReachesLevelThree(GameData gameData, Permanent permanent, int previousLevel) {
        int levelThreeThreshold = permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD).stream()
                .filter(ActivatedAbility::isLevelUpAbility)
                .count() > 1 ? 2 : 3;
        if (previousLevel >= levelThreeThreshold
                || permanent.getCounterCount(CounterType.LEVEL) < levelThreeThreshold) {
            return;
        }
        levelReached(gameData, permanent, REACHES_LEVEL_THREE, "reaches level 3");
    }

    private void levelReached(GameData gameData, Permanent permanent, Trigger watched, String eventDescription) {
        UUID controllerId = gameData.findControllerOf(permanent);
        if (controllerId == null) {
            return;
        }
        // Each "when this Class reaches level N" ability, whole (CR 603.3); a target or graveyard card is chosen as it
        // goes on the stack (CR 603.3d).
        for (TriggeredAbility ability : abilities.abilities(gameData, permanent, watched)) {
            putPlacementAbility(gameData, ability, permanent, controllerId,
                    ConditionContext.forPermanent(permanent, controllerId), null, entry -> {
                    });
            log.info("Game {} - {} {} trigger fires", gameData.id, permanent.getCard().getName(), eventDescription);
        }
    }

    /**
     * "Whenever you put one or more +1/+1 counters on another Hero" once per watcher for one event that put counters
     * on all of {@code targets} at once.
     */
    private void plusOneOnOtherHero(GameData gameData, List<Permanent> targets, int count, UUID placingPlayerId) {
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
        for (Permanent source : new ArrayList<>(battlefield)) {
            if (heroTargets.stream().allMatch(target -> target.getId().equals(source.getId()))) {
                continue;
            }
            for (TriggeredAbility ability : abilities.abilities(gameData, source, PLUS_ONE_ON_OTHER_HERO)) {
                CounterTrigger trigger = counterTrigger(ability, PLUS_ONE_ON_OTHER_HERO);
                requireNoQualifier(trigger, ability, PLUS_ONE_ON_OTHER_HERO);
                requireWatchedEvent(trigger, ability, PLUS_ONE_ON_OTHER_HERO);
                putPlacementAbility(gameData, ability, source, placingPlayerId,
                        ConditionContext.forPermanent(source, placingPlayerId).withEventValue(count), null,
                        entry -> entry.setEventValue(count));
            }
        }
    }

    /** "Whenever you put one or more +1/+1 counters on another [colorless] creature" (Omarthis). */
    private void youPutPlusOneOnAnotherCreature(GameData gameData, Permanent target, int count,
                                                UUID placingPlayerId) {
        if (target == null || count <= 0 || placingPlayerId == null
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
            for (TriggeredAbility ability : abilities.abilities(gameData, source,
                    YOU_PUT_PLUS_ONE_ON_ANOTHER_CREATURE)) {
                CounterTrigger trigger = counterTrigger(ability, YOU_PUT_PLUS_ONE_ON_ANOTHER_CREATURE);
                requireWatchedEvent(trigger, ability, YOU_PUT_PLUS_ONE_ON_ANOTHER_CREATURE);
                if (trigger.qualifier() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, target,
                        trigger.qualifier())) {
                    continue;
                }
                putPlacementAbility(gameData, ability, source, placingPlayerId,
                        ConditionContext.forPermanent(source, placingPlayerId).withEventValue(count), null,
                        entry -> entry.setEventValue(count));
            }
        }
    }

    /**
     * A +1/+1 counter placement on {@code target}: its own "whenever one or more +1/+1 counters are put on this", its
     * controller's "on another [non-Hydra] creature you control" (but {@code excludedSources}'), and "on a Human you
     * control". The number of counters put is the event's value; a placement that names none is one.
     */
    private void plusOnePlaced(GameData gameData, Permanent target, UUID placingPlayerId,
                               List<Permanent> excludedSources, int count) {
        plusOneOnSelf(gameData, target, placingPlayerId, count);
        int placed = Math.max(1, count);
        plusOneOnAnotherNonHydra(gameData, target, placed, placingPlayerId, excludedSources);
        plusOneOnHuman(gameData, target, placingPlayerId, placed);
    }

    /**
     * {@code target}'s own +1/+1 counter abilities: "whenever a +1/+1 counter is put on this" (Fathom Mage, Bloodcrazed
     * Hoplite) once for each counter, "whenever you put one or more +1/+1 counters on this" (Exemplar of Light) only
     * when its controller puts them, "whenever one or more +1/+1 counters are put on this" once.
     */
    private void plusOneOnSelf(GameData gameData, Permanent target, UUID placingPlayerId, int count) {
        if (target == null) {
            return;
        }
        List<TriggeredAbility> found = abilities.abilities(gameData, target, PLUS_ONE_ON_THIS);
        if (found.isEmpty()) {
            return;
        }
        UUID controllerId = gameData.findControllerOf(target);
        if (controllerId == null) {
            return;
        }
        for (TriggeredAbility ability : found) {
            CounterTrigger trigger = counterTrigger(ability, PLUS_ONE_ON_THIS);
            requireNoQualifier(trigger, ability, PLUS_ONE_ON_THIS);
            if (trigger.event() == CounterTrigger.CounterEvent.EACH_PLUS_ONE_COUNTER_PUT) {
                for (int i = 0; i < count; i++) {
                    putPlacementAbility(gameData, ability, target, controllerId,
                            ConditionContext.forPermanent(target, controllerId), null, entry -> {
                            });
                }
            } else if (!byController(trigger) || controllerId.equals(placingPlayerId)) {
                putPlacementAbility(gameData, ability, target, controllerId,
                        ConditionContext.forPermanent(target, controllerId), null, entry -> {
                        });
            }
        }
    }

    /**
     * "Whenever one or more +1/+1 counters are put on another creature you control" and, unless it is a Hydra, "on
     * another non-Hydra creature you control" (Wildwood Scourge).
     */
    private void plusOneOnAnotherNonHydra(GameData gameData, Permanent target, int count, UUID placingPlayerId,
                                          List<Permanent> excludedSources) {
        if (count <= 0 || target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        putAnotherCreatureCounterAbilities(gameData, target, count, placingPlayerId, excludedSources,
                PLUS_ONE_ON_ANOTHER_CREATURE_YOU_CONTROL);
        if (predicateEvaluationService.matchesPermanentPredicate(target,
                new PermanentHasSubtypePredicate(CardSubtype.HYDRA), FilterContext.of(gameData))) {
            return;
        }
        putAnotherCreatureCounterAbilities(gameData, target, count, placingPlayerId, excludedSources,
                PLUS_ONE_ON_ANOTHER_NON_HYDRA);
    }

    /**
     * Hands each ability watching {@code watched} of the permanents of {@code target}'s controller other than
     * {@code target} and {@code excludedSources} to the stack door, recording the +1/+1 counters put on {@code target}.
     */
    private void putAnotherCreatureCounterAbilities(GameData gameData, Permanent target, int count,
                                                    UUID placingPlayerId, List<Permanent> excludedSources,
                                                    Trigger watched) {
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) {
            return;
        }
        Set<UUID> excludedSourceIds = excludedSources.stream().map(Permanent::getId).collect(Collectors.toSet());
        for (Permanent watcher : new ArrayList<>(battlefield)) {
            if (watcher.getId().equals(target.getId()) || excludedSourceIds.contains(watcher.getId())) {
                continue;
            }
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, watched)) {
                requireNothingToJudge(ability, watched);
                putPlacementAbility(gameData, ability, watcher, targetControllerId,
                        ConditionContext.forPermanent(watcher, targetControllerId)
                                .withTriggeringPermanentId(target.getId()).withEventValue(count), null,
                        countersPutRecord(watcher, target, CounterType.PLUS_ONE_PLUS_ONE, count, placingPlayerId));
            }
        }
    }

    /** "Whenever one or more +1/+1 counters are put on a Human you control". */
    private void plusOneOnHuman(GameData gameData, Permanent target, UUID placingPlayerId, int count) {
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
        for (Permanent source : new ArrayList<>(battlefield)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, source, PLUS_ONE_ON_HUMAN)) {
                CounterTrigger trigger = counterTrigger(ability, PLUS_ONE_ON_HUMAN);
                requireNoQualifier(trigger, ability, PLUS_ONE_ON_HUMAN);
                requireWatchedEvent(trigger, ability, PLUS_ONE_ON_HUMAN);
                putPlacementAbility(gameData, ability, source, controllerId,
                        ConditionContext.forPermanent(source, controllerId)
                                .withTriggeringPermanentId(target.getId()).withEventValue(count), null,
                        countersPutRecord(source, target, CounterType.PLUS_ONE_PLUS_ONE, count, placingPlayerId));
            }
        }
    }

    /**
     * {@code count} +1/+1 counters put on {@code target}, which {@code controllerId} controls, by
     * {@code placingPlayerId}: "whenever you put one or more +1/+1 counters on a permanent / creature you control",
     * "whenever one or more +1/+1 counters are put on a creature you control", "whenever you put one or more +1/+1
     * counters on a creature", and, for the first placement on {@code target} this turn, "on another permanent you
     * control, if it's the first time".
     */
    private void plusOneOnControlledPermanent(GameData gameData, Permanent target, UUID controllerId, int count,
                                              UUID placingPlayerId, boolean firstPlacementOnThisPermanent) {
        if (controllerId.equals(placingPlayerId)) {
            plusOneOnControlled(gameData, controllerId, count, YOU_PUT_PLUS_ONE_ON_PERMANENT);
        }
        if (gameQueryService.isCreature(gameData, target)) {
            if (controllerId.equals(placingPlayerId)) {
                plusOneOnControlled(gameData, controllerId, count, YOU_PUT_PLUS_ONE_ON_CREATURE_YOU_CONTROL);
            }
            plusOneOnControlled(gameData, controllerId, count, PLUS_ONE_ON_CREATURE_YOU_CONTROL);
            if (count > 0 && placingPlayerId != null) {
                plusOneOnControlled(gameData, placingPlayerId, count, YOU_PUT_PLUS_ONE_ON_ANY_CREATURE);
            }
        }
        if (firstPlacementOnThisPermanent) {
            plusOneFirstTimeOnAnotherPermanent(gameData, target, controllerId, count, placingPlayerId);
        }
    }

    private void plusOneFirstTimeOnAnotherPermanent(GameData gameData, Permanent target, UUID controllerId, int count,
                                                    UUID placingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.getId().equals(target.getId())) {
                continue;
            }
            for (TriggeredAbility ability : abilities.abilities(gameData, source,
                    PLUS_ONE_ON_ANOTHER_PERMANENT_FIRST_TIME)) {
                requireNothingToJudge(ability, PLUS_ONE_ON_ANOTHER_PERMANENT_FIRST_TIME);
                putPlacementAbility(gameData, ability, source, controllerId,
                        ConditionContext.forPermanent(source, controllerId)
                                .withTriggeringPermanentId(target.getId()).withEventValue(count), null,
                        countersPutRecord(source, target, CounterType.PLUS_ONE_PLUS_ONE, count, placingPlayerId));
            }
        }
    }

    /**
     * The abilities watching {@code watched} of {@code controllerId}'s permanents: the number of counters and the
     * source as it triggered, whatever the effects are; the door marks "do this only once each turn" as the action is
     * taken (CR 603.2h).
     */
    private void plusOneOnControlled(GameData gameData, UUID controllerId, int count, Trigger watched) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, source, watched)) {
                CounterTrigger trigger = counterTrigger(ability, watched);
                requireNoQualifier(trigger, ability, watched);
                requireWatchedEvent(trigger, ability, watched);
                Permanent snapshot = new Permanent(source);
                putPlacementAbility(gameData, ability, source, controllerId,
                        ConditionContext.forPermanent(source, controllerId).withEventValue(count), null, entry -> {
                            entry.setEventValue(count);
                            entry.setSourcePermanentSnapshot(snapshot);
                        });
            }
        }
    }

    /**
     * "Whenever one or more +1/+1 counters are put on a creature you control" of the cards in the creature's
     * controller's graveyard: the record names this graveyard incarnation of the card as its triggering card and its
     * source (CR 400.7).
     */
    private void graveyardPlusOneOnCreature(GameData gameData, Permanent creature, UUID creatureControllerId,
                                            int amount) {
        if (creature == null || creatureControllerId == null || amount <= 0
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        List<Card> graveyard = gameData.playerGraveyards.get(creatureControllerId);
        if (graveyard == null) {
            return;
        }
        for (Card card : new ArrayList<>(graveyard)) {
            putGraveyardAbilities(gameData, card, creatureControllerId, PLUS_ONE_ON_CREATURE_YOU_CONTROL);
        }
    }

    /**
     * "Whenever one or more counters are put on a creature you control" (and "you put", Mikey & Leo): once for each
     * counter; "if it's the first time counters have been put on that creature this turn" (Stalwart Successor) is
     * {@code firstCountersThisTurn}, for each creature.
     */
    private void onControlledCreature(GameData gameData, Permanent creature, UUID controllerId, int count,
                                      UUID placingPlayerId, boolean firstCountersThisTurn) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            List<TriggeredAbility> found = abilities.abilities(gameData, source, COUNTERS_ON_CREATURE_YOU_CONTROL);
            for (int i = 0; i < count; i++) {
                for (TriggeredAbility ability : found) {
                    CounterTrigger trigger = counterTrigger(ability, COUNTERS_ON_CREATURE_YOU_CONTROL);
                    if (trigger.event() == CounterTrigger.CounterEvent.YOU_PUT_COUNTERS
                            && !Objects.equals(placingPlayerId, controllerId)) {
                        continue;
                    }
                    if (trigger.qualifier() != null && !predicateEvaluationService.matchesPermanentPredicate(
                            gameData, creature, trigger.qualifier())) {
                        continue;
                    }
                    if (ability.options().limit() == TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE
                            && !firstCountersThisTurn) {
                        continue;
                    }
                    putPlacementAbility(gameData, ability, source, controllerId,
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
     * {@code count} -1/-1 counters put on {@code creature} by {@code placingPlayerId}: "whenever a -1/-1 counter is put
     * on a creature" (Flourishing Defenses) and "whenever you put a -1/-1 counter on a creature" (Nest of Scarabs) once
     * for each counter, "whenever one or more -1/-1 counters are put on a creature" (Auntie Ool) and "whenever you put
     * one or more -1/-1 counters on a creature" (Hapatra) once, then the creature's own "whenever you put one or more
     * -1/-1 counters on this creature" (Defiant Greatmaw) when its controller put them.
     */
    private void minusOnePlaced(GameData gameData, Permanent creature, int count, UUID placingPlayerId) {
        if (count <= 0 || creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            boolean placedByThisController = controllerId.equals(placingPlayerId);
            for (Permanent source : new ArrayList<>(battlefield)) {
                List<TriggeredAbility> perCounterAbilities = new ArrayList<>(abilities.abilities(gameData, source,
                        MINUS_ONE_COUNTER_ON_CREATURE));
                if (placedByThisController) {
                    perCounterAbilities.addAll(abilities.abilities(gameData, source, YOU_PUT_MINUS_ONE_COUNTER));
                }
                for (int i = 0; i < count; i++) {
                    for (TriggeredAbility ability : perCounterAbilities) {
                        putPlacementAbility(gameData, ability, source, controllerId,
                                ConditionContext.forPermanent(source, controllerId)
                                        .withTriggeringPermanentId(creature.getId()).withEventValue(1), null,
                                countersPutRecord(source, creature, CounterType.MINUS_ONE_MINUS_ONE, 1,
                                        placingPlayerId));
                    }
                }
                // The affected creature is the ability's non-targeting "that creature", with its controller at trigger
                // time retained for zone changes.
                for (TriggeredAbility ability : abilities.abilities(gameData, source, MINUS_ONE_COUNTERS_ON_CREATURE)) {
                    putPlacementAbility(gameData, ability, source, controllerId,
                            ConditionContext.forPermanent(source, controllerId)
                                    .withTriggeringPermanentId(creature.getId()).withEventValue(count), null,
                            entry -> {
                                entry.setTargetId(creature.getId());
                                entry.setTriggeringPermanentId(creature.getId());
                                entry.setTriggeringPermanentControllerId(creatureControllerId);
                                entry.setNonTargeting(true);
                            });
                }
                if (placedByThisController) {
                    for (TriggeredAbility ability : abilities.abilities(gameData, source,
                            YOU_PUT_MINUS_ONE_COUNTERS)) {
                        putPlacementAbility(gameData, ability, source, controllerId,
                                ConditionContext.forPermanent(source, controllerId)
                                        .withTriggeringPermanentId(creature.getId()).withEventValue(count), null,
                                entry -> entry.setEventValue(count));
                    }
                }
            }
        });
        UUID controllerId = gameData.findControllerOf(creature);
        if (controllerId == null || !controllerId.equals(placingPlayerId)) {
            return;
        }
        for (TriggeredAbility ability : abilities.abilities(gameData, creature, YOU_PUT_MINUS_ONE_ON_THIS)) {
            putPlacementAbility(gameData, ability, creature, controllerId,
                    ConditionContext.forPermanent(creature, controllerId), null, entry -> {
                    });
        }
    }

    /**
     * "Whenever one or more loyalty counters are put on planeswalkers you control": the amount is the event's value,
     * so "that many" is the placement's.
     */
    private void loyaltyOnPlaneswalkers(GameData gameData, UUID controllerId, int count) {
        if (controllerId == null || count <= 0) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, source, LOYALTY_ON_PLANESWALKERS)) {
                requireNothingToJudge(ability, LOYALTY_ON_PLANESWALKERS);
                putPlacementAbility(gameData, ability, source, controllerId,
                        ConditionContext.forPermanent(source, controllerId).withEventValue(count), null,
                        entry -> entry.setEventValue(count));
            }
        }
    }

    /**
     * "When you remove the last [kind] counter from this permanent": {@code removingPlayerId} removed {@code removed}
     * counters of {@code counterType}, none is left, and they control it.
     */
    private void lastRemoved(GameData gameData, Permanent source, CounterType counterType, int removed,
                             UUID removingPlayerId) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (removed <= 0 || source.getCounterCount(counterType) != 0
                || !Objects.equals(controllerId, removingPlayerId)) {
            return;
        }
        Permanent snapshot = new Permanent(source);
        for (TriggeredAbility ability : abilities.abilities(gameData, source, YOU_REMOVE_LAST_COUNTER)) {
            CounterTrigger trigger = counterTrigger(ability, YOU_REMOVE_LAST_COUNTER);
            requireNoQualifier(trigger, ability, YOU_REMOVE_LAST_COUNTER);
            if (trigger.counterKind() != counterType) {
                continue;
            }
            putPlacementAbility(gameData, ability, source, controllerId,
                    ConditionContext.forPermanent(source, controllerId), null, entry -> {
                        entry.setSourcePermanentSnapshot(snapshot);
                        entry.setEventCounterType(counterType);
                        entry.setEventValue(removed);
                    });
        }
    }

    /**
     * The counters removed from each permanent since the abilities last looked: "whenever one or more counters are
     * removed from a permanent you control" (Cloudsculpt Armorer), "whenever one or more time counters are removed
     * from this", "whenever a +1/+1 counter is removed from this" once for each counter, and "whenever one or more
     * loyalty counters are removed from this" (Chandra, Fire Artisan), each permanent's in that order.
     */
    private void removed(GameData gameData) {
        gameData.forEachPermanent((controllerId, permanent) -> {
            int removedCounters = permanent.drainCountersRemovedSinceTriggerCheck();
            if (removedCounters > 0) {
                for (Permanent watcher : new ArrayList<>(
                        gameData.playerBattlefields.getOrDefault(controllerId, List.of()))) {
                    putUnjudgedAbilities(gameData, watcher, controllerId, COUNTERS_REMOVED_FROM_PERMANENT_YOU_CONTROL,
                            ConditionContext.forPermanent(watcher, controllerId).withEventValue(removedCounters),
                            EventAbilitySupport.eventRecord(watcher, null, controllerId, removedCounters, permanent,
                                    controllerId));
                }
            }
            int timeCountersRemoved = permanent.drainTimeCountersRemovedSinceTriggerCheck();
            if (timeCountersRemoved > 0) {
                putUnjudgedAbilities(gameData, permanent, controllerId, TIME_COUNTERS_REMOVED,
                        ConditionContext.forPermanent(permanent, controllerId).withEventValue(timeCountersRemoved),
                        EventAbilitySupport.eventRecord(permanent, null, controllerId, timeCountersRemoved, null,
                                null));
            }
            int plusOneRemoved = permanent.drainPlusOnePlusOneCountersRemovedSinceTriggerCheck();
            for (int i = 0; i < plusOneRemoved; i++) {
                putUnjudgedAbilities(gameData, permanent, controllerId, PLUS_ONE_COUNTER_REMOVED,
                        ConditionContext.forPermanent(permanent, controllerId),
                        EventAbilitySupport.eventRecord(permanent, null, controllerId, null, null, null));
            }
            int amount = permanent.drainLoyaltyCountersRemovedSinceTriggerCheck();
            if (amount <= 0) return;
            putUnjudgedAbilities(gameData, permanent, controllerId, LOYALTY_COUNTERS_REMOVED,
                    ConditionContext.forPermanent(permanent, controllerId).withEventValue(amount),
                    EventAbilitySupport.eventRecord(permanent, null, controllerId, amount, permanent, controllerId));
        });
    }

    /**
     * "Whenever a time counter is removed from this card while it's exiled" and "whenever you remove a time counter
     * from this card while it's exiled" (Watcher of Hours, only when its owner removes it): its event condition ("when
     * the last time counter is removed", CR 603.2) and intervening "if" (CR 603.4) are read from the exiled card.
     */
    private void timeCounterRemovedInExile(GameData gameData, Card card, UUID ownerId, int remainingCounters,
                                           UUID removerId) {
        if (card == null || ownerId == null || remainingCounters < 0) {
            return;
        }
        for (TriggeredAbility ability : abilities.exileAbilities(gameData, card, TIME_COUNTER_REMOVED_IN_EXILE)) {
            CounterTrigger trigger = counterTrigger(ability, TIME_COUNTER_REMOVED_IN_EXILE);
            if (trigger.qualifier() != null || ability.options().limit() != TriggerLimit.NONE) {
                throw new IllegalStateException("The matcher of " + TIME_COUNTER_REMOVED_IN_EXILE
                        + " judges no filter or limit: " + ability);
            }
            if (trigger.event() == CounterTrigger.CounterEvent.YOU_REMOVE_TIME_COUNTER_IN_EXILE
                    && removerId != null && !removerId.equals(ownerId)) {
                continue;
            }
            abilities.put(gameData, ability, card, null, ownerId, ConditionContext.forCard(card, ownerId),
                    TriggerRecord.NONE);
        }
    }

    /**
     * "When this creature becomes renowned" (Relic Seeker) and "whenever a [filter] creature you control becomes
     * renowned" (Valeron Wardens): renown flips the creature from not renowned to renowned once (CR 702.112c).
     */
    private void renowned(GameData gameData, Permanent renownedCreature, UUID controllerId) {
        putUnjudgedAbilities(gameData, renownedCreature, controllerId, BECOMES_RENOWNED,
                ConditionContext.forPermanent(renownedCreature, controllerId),
                EventAbilitySupport.eventRecord(renownedCreature, null, controllerId, null, null, null));
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            for (TriggeredAbility ability : abilities.abilities(gameData, perm, CREATURE_BECOMES_RENOWNED)) {
                PermanentPredicate filter = counterTrigger(ability, CREATURE_BECOMES_RENOWNED).qualifier();
                if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(renownedCreature, filter,
                        FilterContext.of(gameData)
                                .withSourceCardId(perm.getOriginalCard().getId())
                                .withSourceControllerId(ownerId))) {
                    continue;
                }
                abilities.put(gameData, ability, perm.getCard(), perm, ownerId,
                        ConditionContext.forPermanent(perm, ownerId).withTriggeringPermanentId(renownedCreature.getId()),
                        EventAbilitySupport.eventRecord(perm, null, ownerId, null, renownedCreature, controllerId));
            }
        });
    }

    /**
     * Each "when this becomes monstrous" ability of {@code monstrousPermanent}, whole (CR 603.3), the X its monstrosity
     * was activated with as the entry's X ("when this becomes monstrous, it deals X damage").
     */
    private void monstrous(GameData gameData, Permanent monstrousPermanent, UUID controllerId, int xValue) {
        Permanent snapshot = new Permanent(monstrousPermanent);
        putUnjudgedAbilities(gameData, monstrousPermanent, controllerId, BECOMES_MONSTROUS,
                ConditionContext.forPermanent(monstrousPermanent, controllerId).withXValue(xValue), entry -> {
                    entry.setXValue(xValue);
                    entry.setSourcePermanentSnapshot(snapshot);
                });
    }

    /** "Whenever this creature trains" (CR 702.149b): each ability, whole, as the training ability. */
    private void trained(GameData gameData, Permanent source, UUID controllerId) {
        Card card = source.getCard();
        for (TriggeredAbility ability : abilities.abilities(gameData, source, TRAINS)) {
            abilities.put(gameData, ability, card, source, controllerId,
                    ConditionContext.forPermanent(source, controllerId), TriggerRecord.NONE, null,
                    card.getName() + "'s training ability", GameLog.abilityTriggers(card));
        }
    }

    /**
     * Each ability of {@code source} watching {@code watched} ("whenever this creature evolves", "when this Class
     * becomes level N"), whole, controlled by {@code controllerId}; a target or graveyard card is chosen as it goes on
     * the stack (CR 603.3d).
     */
    private void putOwnAbilities(GameData gameData, Permanent source, UUID controllerId, Trigger watched) {
        for (TriggeredAbility ability : abilities.abilities(gameData, source, watched)) {
            putPlacementAbility(gameData, ability, source, controllerId,
                    ConditionContext.forPermanent(source, controllerId), null, entry -> {
                    });
        }
    }

    /**
     * "Whenever you get one or more {E}" (Territorial Gorger, Fabrication Module's target, Brotherhood Scribe's
     * intervening "if"): the player and the energy gained ("that much").
     */
    private void energy(GameData gameData, UUID gainingPlayerId, int energyGainedAmount) {
        if (energyGainedAmount <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(gainingPlayerId);
        if (battlefield == null) return;
        for (Permanent perm : List.copyOf(battlefield)) {
            putUnjudgedAbilities(gameData, perm, gainingPlayerId, GETS_ENERGY,
                    ConditionContext.forPermanent(perm, gainingPlayerId).withEventValue(energyGainedAmount),
                    EventAbilitySupport.eventRecord(perm, null, gainingPlayerId, energyGainedAmount, null, null));
        }
    }

    /** "Whenever you proliferate", once for each proliferation, of the permanents and then the graveyard's cards. */
    private void proliferated(GameData gameData, UUID proliferatingPlayerId, int proliferateCount) {
        if (proliferateCount <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(proliferatingPlayerId);
        if (battlefield != null) {
            for (Permanent perm : List.copyOf(battlefield)) {
                for (int i = 0; i < proliferateCount; i++) {
                    putUnjudgedAbilities(gameData, perm, proliferatingPlayerId, PROLIFERATES,
                            ConditionContext.forPermanent(perm, proliferatingPlayerId),
                            EventAbilitySupport.eventRecord(perm, null, proliferatingPlayerId, null, null, null));
                }
            }
        }
        List<Card> graveyard = gameData.playerGraveyards.get(proliferatingPlayerId);
        if (graveyard == null) return;
        for (Card card : new ArrayList<>(graveyard)) {
            for (int i = 0; i < proliferateCount; i++) {
                putGraveyardAbilities(gameData, card, proliferatingPlayerId, PROLIFERATES);
            }
        }
    }

    /**
     * Hands each ability of {@code perm} watching {@code watched} to the stack door, judged in {@code context} and
     * stamped with {@code record}. These matchers judge no filter or narrower event of their own; this throws on one.
     */
    private void putUnjudgedAbilities(GameData gameData, Permanent perm, UUID controllerId, Trigger watched,
                                      ConditionContext context, TriggerRecord record) {
        for (TriggeredAbility ability : abilities.abilities(gameData, perm, watched)) {
            CounterTrigger trigger = counterTrigger(ability, watched);
            if (trigger.qualifier() != null || trigger.event() != ((CounterTrigger) watched).event()) {
                throw new IllegalStateException("The matcher of " + watched + " judges no filter or narrower event: "
                        + ability);
            }
            abilities.put(gameData, ability, perm.getCard(), perm, controllerId, context, record);
        }
    }

    /**
     * Hands each ability of {@code card} in its owner {@code controllerId}'s graveyard watching {@code watched} to the
     * stack door: the record names this graveyard incarnation of the card as its triggering card and its source
     * (CR 400.7). No graveyard counter ability is written with a filter or narrower event; this throws on one.
     */
    private void putGraveyardAbilities(GameData gameData, Card card, UUID controllerId, Trigger watched) {
        for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, watched)) {
            CounterTrigger trigger = counterTrigger(ability, watched);
            if (trigger.qualifier() != null || trigger.event() != ((CounterTrigger) watched).event()) {
                throw new IllegalStateException("The matcher of " + watched + " judges no filter or narrower event: "
                        + ability);
            }
            long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
            abilities.put(gameData, ability, card, null, controllerId, ConditionContext.forCard(card, controllerId),
                    entry -> {
                        entry.setTriggeringCardId(card.getId());
                        entry.setTriggeringCardGraveyardEntryVersion(graveyardEntryVersion);
                        entry.setSourceGraveyardEntryVersion(graveyardEntryVersion);
                    });
        }
    }

    /**
     * Hands {@code ability}, a counter-placement ability of {@code source} controlled by {@code controllerId}, to the
     * stack door: its event condition is judged now in {@code context} (CR 603.2), its intervening "if" and limit
     * ("once each turn for each creature" by {@code limitCreatureId}) by the door, which stamps {@code record} and
     * chooses its targets as it goes on the stack (CR 603.3d).
     */
    private boolean putPlacementAbility(GameData gameData, TriggeredAbility ability, Permanent source,
                                        UUID controllerId, ConditionContext context, UUID limitCreatureId,
                                        TriggerRecord record) {
        Card card = source.getCard();
        return abilities.put(gameData, ability, card, source, controllerId, context, record, limitCreatureId,
                card.getName() + "'s triggered ability", GameLog.cardThen(card, "'s triggered ability triggers."));
    }

    /**
     * Whether {@code condition}, an ability's event condition ({@code null} for none), holds as counters are put on
     * {@code target}: a counter threshold ("when the [Nth] counter is put on this") holds only for the counters that
     * cross it, any other condition as it is now.
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

    /** Whether only the ability's controller puts the counters ("you put one or more counters on …"). */
    private static boolean byController(CounterTrigger trigger) {
        return trigger.event() == CounterTrigger.CounterEvent.YOU_PUT_COUNTERS
                || trigger.event() == CounterTrigger.CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS;
    }

    /**
     * {@code ability}'s counter condition watched as {@code watched}, its trigger-level parts (the filter on the
     * permanent, the narrower event, the kind of counter) for the matcher to judge; this throws on "once for each"
     * other than each event, which no counter matcher reads.
     */
    static CounterTrigger counterTrigger(TriggeredAbility ability, Trigger watched) {
        CounterTrigger condition = EventAbilitySupport.condition(ability, CounterTrigger.class, watched);
        if (condition == null) {
            throw new IllegalArgumentException(ability + " has no counter condition watched as " + watched);
        }
        if (ability.options().multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("No counter matcher reads " + ability.options().multiplicity() + ": "
                    + ability);
        }
        return condition;
    }

    private static void requireNoQualifier(CounterTrigger trigger, TriggeredAbility ability, Trigger watched) {
        if (trigger.qualifier() != null) {
            throw new IllegalStateException("The matcher of " + watched + " judges no filter: " + ability);
        }
    }

    private static void requireWatchedEvent(CounterTrigger trigger, TriggeredAbility ability, Trigger watched) {
        if (trigger.event() != ((CounterTrigger) watched).event()) {
            throw new IllegalStateException("The matcher of " + watched + " reads no " + trigger.event() + ": "
                    + ability);
        }
    }

    /** Throws when {@code ability} has a filter, a narrower event or a limit, none of which its matcher judges. */
    private static void requireNothingToJudge(TriggeredAbility ability, Trigger watched) {
        CounterTrigger trigger = counterTrigger(ability, watched);
        requireNoQualifier(trigger, ability, watched);
        requireWatchedEvent(trigger, ability, watched);
        if (ability.options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The matcher of " + watched + " judges no limit: " + ability);
        }
    }
}
