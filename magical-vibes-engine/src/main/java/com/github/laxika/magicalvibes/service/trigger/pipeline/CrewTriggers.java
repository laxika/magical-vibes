package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.rules.CrewEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Triggers the crew, saddle and station abilities of a {@link CrewEvent} (CR 603.2), each handed whole to the stack
 * door with the record of the event. The abilities are read from {@link TriggerIndex} as the layers leave them; an
 * ability's event condition, its crewing creature's filter and "becomes crewed / saddled for the first time each turn"
 * are judged here, its intervening "if" (CR 603.4) and limit (CR 603.2h) by the door.
 *
 * <p>The abilities that trigger as a creature is tapped to pay a crew, saddle or station cost (CR 702.122a, 702.171a,
 * 702.184a) are held until the activated ability is on the stack, above which they go the next time a player would
 * receive priority (CR 603.3).
 */
@Service
public class CrewTriggers {

    private static final Trigger CREWS_VEHICLE = Triggers.crewsVehicle();
    private static final Trigger CREATURE_YOU_CONTROL_CREWS_VEHICLE = Triggers.creatureYouControlCrewsVehicle();
    private static final Trigger SADDLES_OR_CREWS_DURING_MAIN_PHASE = Triggers.saddlesOrCrewsDuringMainPhase();
    private static final Trigger BECOMES_STATIONED = Triggers.becomesStationed();
    private static final Trigger BECOMES_CREWED = Triggers.becomesCrewed();
    private static final Trigger BECOMES_SADDLED = Triggers.becomesSaddled();

    private final EventAbilitySupport abilities;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    public CrewTriggers(EventAbilitySupport abilities, @Lazy GameQueryService gameQueryService,
                        @Lazy PredicateEvaluationService predicateEvaluationService) {
        this.abilities = abilities;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, CrewEvent event) {
        switch (event) {
            case CrewEvent.CreatureTappedToCrewOrSaddle tapped -> {
                crewsVehicle(gameData, tapped.creature(), tapped.permanent());
                creatureYouControlCrewsVehicle(gameData, tapped.creature(), tapped.permanent());
                saddlesOrCrewsDuringMainPhase(gameData, tapped);
            }
            case CrewEvent.CreatureStations stations -> holdForActivation(gameData, () -> {
                Permanent spacecraft = stations.spacecraft();
                UUID stationingCardId = stations.creature().getCard().getId();
                putOwnAbilities(gameData, spacecraft, stations.controllerId(), BECOMES_STATIONED,
                        entry -> entry.setTargetId(stationingCardId));
            });
            case CrewEvent.BecameCrewed crewed -> putOwnAbilities(gameData, crewed.vehicle(), crewed.controllerId(),
                    BECOMES_CREWED, TriggerRecord.NONE);
            case CrewEvent.BecameSaddled saddled -> putOwnAbilities(gameData, saddled.mount(),
                    saddled.controllerId(), BECOMES_SADDLED, TriggerRecord.NONE);
        }
    }

    /** The crewing creature's own "whenever this creature crews a Vehicle". */
    private void crewsVehicle(GameData gameData, Permanent creature, Permanent vehicle) {
        if (!isVehicle(vehicle)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) {
            return;
        }
        holdForActivation(gameData, () -> putOwnAbilities(gameData, creature, controllerId, CREWS_VEHICLE,
                crewedRecord(vehicle)));
    }

    /**
     * Its controller's "whenever a creature you control crews a Vehicle", narrowed by the trigger's object filter
     * judged against the crewing creature from the watcher ("this or another legendary creature you control").
     */
    private void creatureYouControlCrewsVehicle(GameData gameData, Permanent creature, Permanent vehicle) {
        if (!isVehicle(vehicle)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        List<Permanent> battlefield = controllerId == null ? null : gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        holdForActivation(gameData, () -> {
            for (Permanent watcher : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : abilities.abilities(gameData, watcher,
                        CREATURE_YOU_CONTROL_CREWS_VEHICLE)) {
                    ObjectActionTrigger condition = EventAbilitySupport.condition(ability, ObjectActionTrigger.class,
                            CREATURE_YOU_CONTROL_CREWS_VEHICLE);
                    if (condition.objectFilter() != null && !predicateEvaluationService.matchesPermanentPredicate(
                            creature, condition.objectFilter(), FilterContext.of(gameData)
                                    .withSourceCardId(watcher.getCard().getId())
                                    .withSourceControllerId(controllerId)
                                    .withSourcePermanentId(watcher.getId())
                                    .withSourcePermanentSnapshot(watcher))) {
                        continue;
                    }
                    putJudged(gameData, watcher, controllerId, ability, crewedRecord(vehicle));
                }
            }
        });
    }

    /**
     * The tapped creature's own "whenever this creature saddles a Mount or crews a Vehicle during your main phase": the
     * activating player's main phase, a Mount or Vehicle as the layers leave it, the Mount or Vehicle as the triggering
     * permanent. It is judged once for each creature tapped to pay the cost.
     */
    private void saddlesOrCrewsDuringMainPhase(GameData gameData, CrewEvent.CreatureTappedToCrewOrSaddle event) {
        UUID activatingPlayerId = event.activatingPlayerId();
        if (!activatingPlayerId.equals(gameData.activePlayerId)
                || (gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN)
                || event.permanent() == null) {
            return;
        }
        Permanent saddledOrCrewed = gameQueryService.findPermanentById(gameData, event.permanent().getId());
        if (saddledOrCrewed == null
                || !gameQueryService.hasEffectiveSubtype(gameData, saddledOrCrewed, CardSubtype.MOUNT)
                && !gameQueryService.hasEffectiveSubtype(gameData, saddledOrCrewed, CardSubtype.VEHICLE)) {
            return;
        }
        Permanent watcher = gameData.playerBattlefields.getOrDefault(activatingPlayerId, List.of()).stream()
                .filter(permanent -> permanent.getId().equals(event.creature().getId()))
                .findFirst().orElse(null);
        if (watcher == null) {
            return;
        }
        holdForActivation(gameData, () -> putOwnAbilities(gameData, watcher, activatingPlayerId,
                SADDLES_OR_CREWS_DURING_MAIN_PHASE, crewedRecord(saddledOrCrewed)));
    }

    private static boolean isVehicle(Permanent vehicle) {
        return vehicle != null && vehicle.getCard().getSubtypes().contains(CardSubtype.VEHICLE);
    }

    /** The Vehicle or Mount as the triggering permanent ("that Vehicle"). */
    private static TriggerRecord crewedRecord(Permanent vehicle) {
        return entry -> {
            entry.setTriggeringPermanent(vehicle);
            entry.setTriggeringPermanentId(vehicle.getId());
        };
    }

    /** Hands each of {@code perm}'s own abilities watched as {@code watched} to the stack door. */
    private void putOwnAbilities(GameData gameData, Permanent perm, UUID controllerId, Trigger watched,
                                 TriggerRecord event) {
        for (TriggeredAbility ability : abilities.abilities(gameData, perm, watched)) {
            putJudged(gameData, perm, controllerId, ability, event);
        }
    }

    /**
     * Hands {@code ability} of {@code perm} to the stack door with the source as it was and {@code event} stamped,
     * judging "becomes crewed / saddled for the first time each turn" (Mindlink Mech, Stubborn Burrowfiend) by a mark of
     * the ability's own, so no other ability of the source uses it up.
     */
    private void putJudged(GameData gameData, Permanent perm, UUID controllerId, TriggeredAbility ability,
                           TriggerRecord event) {
        String firstTimeKey = firstTimeEachTurn(ability) ? ability.limitKey() + ":first-time" : null;
        if (firstTimeKey != null && TriggeredAbilityStackService.limitReached(gameData, TriggerLimit.ONCE_EACH_TURN,
                perm.getId(), firstTimeKey)) {
            return;
        }
        Permanent snapshot = new Permanent(perm);
        boolean triggered = abilities.put(gameData, ability, perm.getCard(), perm, controllerId,
                ConditionContext.forPermanent(perm, controllerId), entry -> {
                    entry.setNonTargeting(true);
                    entry.setSourcePermanentSnapshot(snapshot);
                    event.stamp(entry);
                });
        if (triggered && firstTimeKey != null) {
            TriggeredAbilityStackService.markLimit(gameData, TriggerLimit.ONCE_EACH_TURN, perm.getId(),
                    firstTimeKey);
        }
    }

    /** Whether {@code ability} is "becomes crewed / saddled for the first time each turn". */
    private static boolean firstTimeEachTurn(TriggeredAbility ability) {
        return ability.conditions().stream().anyMatch(condition -> condition.trigger()
                instanceof ObjectActionTrigger object
                && (object.action() == ObjectActionTrigger.ObjectAction.BECOMES_CREWED_FOR_FIRST_TIME_EACH_TURN
                || object.action() == ObjectActionTrigger.ObjectAction.BECOMES_SADDLED_FOR_FIRST_TIME_EACH_TURN));
    }

    /**
     * Runs {@code collect}, which hands abilities that trigger as an activated ability's cost is paid to the stack door,
     * and holds what it put on the stack until that ability is on the stack (CR 603.3).
     */
    private static void holdForActivation(GameData gameData, Runnable collect) {
        int before = gameData.stack.size();
        collect.run();
        if (gameData.stack.size() > before) {
            List<StackEntry> held = gameData.stack.subList(before, gameData.stack.size());
            gameData.pendingActivatedAbilityCostTriggers.addAll(new ArrayList<>(held));
            held.clear();
        }
    }
}
