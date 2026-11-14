package com.github.laxika.magicalvibes.service.state;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StateTriggerKey;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.MiscAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.MiscWatch;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements MTG rule 603.8 — state-triggered abilities.
 *
 * <p>After state-based actions run, this service checks every permanent for state-triggered abilities
 * ({@code Triggers.state()}; its game state read by {@link MiscAbilityReader#stateTrigger}) whose condition is met. If
 * the ability is not already on the stack (tracked via {@link GameData#stateTriggerOnStack}), it is handed whole to
 * the stack door ({@link TriggeredAbilityStackService#put}), which judges its intervening "if" (CR 603.4) and chooses
 * its targets. The trigger won't fire again until it resolves, is countered, or otherwise leaves the stack.</p>
 */
@Slf4j
@Service
public class StateTriggerService {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final TriggerCollectionService triggerCollectionService;

    @Autowired
    public StateTriggerService(GameLogService gameLogService,
                               PredicateEvaluationService predicateEvaluationService,
                               GameQueryService gameQueryService,
                               @Lazy TriggeredAbilityStackService triggeredAbilityStackService,
                               @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameLogService = gameLogService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameQueryService = gameQueryService;
        this.triggeredAbilityStackService = triggeredAbilityStackService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /** A service without the stack door, for a test in which no state trigger is met. */
    public StateTriggerService(GameLogService gameLogService,
                               PredicateEvaluationService predicateEvaluationService) {
        this(gameLogService, predicateEvaluationService, null, null, null);
    }

    /**
     * Evaluates a state trigger's condition: a {@code sourcePredicate} goes through the
     * layer-aware predicate evaluator (so static keyword/color/type grants count). A supplied
     * free-form {@link com.github.laxika.magicalvibes.model.effect.StateTriggerPredicate} must also pass.
     */
    private boolean conditionMet(GameData gameData, StateTriggerEffect trigger, Permanent perm, UUID controllerId) {
        if (trigger.predicate() != null && !trigger.predicate().test(gameData, perm, controllerId)) {
            return false;
        }
        if (trigger.sourcePredicate() != null) {
            return predicateEvaluationService.matchesPermanentPredicate(perm, trigger.sourcePredicate(),
                    FilterContext.of(gameData)
                            .withSourceCardId(perm.getCard().getId())
                            .withSourceControllerId(controllerId));
        }
        if (trigger.battlefieldPredicate() != null) {
            int matches = 0;
            FilterContext context = FilterContext.of(gameData)
                    .withSourceCardId(perm.getCard().getId())
                    .withSourceControllerId(controllerId)
                    .withSourcePermanentId(perm.getId());
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (predicateEvaluationService.matchesPermanentPredicate(
                            permanent, trigger.battlefieldPredicate(), context)) {
                        matches++;
                        if (matches > trigger.maximumBattlefieldMatches()) {
                            return false;
                        }
                    }
                }
            }
            return matches >= trigger.minimumBattlefieldMatches();
        }
        return trigger.predicate() != null;
    }

    /**
     * Checks all permanents for state-triggered abilities whose conditions are met. Each qualifying ability not already
     * tracked goes to the stack door whole, one object (CR 603.3), its source the permanent; the entry records the
     * permanent its game state refers to ("that creature", non-targeting) and which of the permanent's state triggers
     * it is. One that triggers is marked in {@link GameData#stateTriggerOnStack}.
     *
     * <p>Iterates in APNAP order (orderedPlayerIds) so triggers are stacked correctly.
     * Nothing triggers once the game has ended.</p>
     */
    public void checkStateTriggers(GameData gameData) {
        if (gameData.status == GameStatus.FINISHED) {
            return;
        }
        checkPlanarStateTriggers(gameData);
        boolean parked = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            // Snapshot to avoid ConcurrentModificationException if a trigger modifies the list
            List<Permanent> snapshot = List.copyOf(battlefield);
            StateTriggerCursor cursor = new StateTriggerCursor();
            StateTriggerFinding finding;
            while ((finding = inQueryScope(gameData,
                    () -> nextStateTrigger(gameData, snapshot, playerId, cursor))) != null) {
                Permanent perm = finding.permanent();
                TriggeredAbility ability = finding.ability();
                StateTriggerKey key = new StateTriggerKey(perm.getId(), finding.index());
                Permanent referencedPermanent = finding.referencedPermanent();
                int effectIndex = finding.index();
                StackEntry[] stamped = new StackEntry[1];
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        perm.getCard(), perm, playerId, description(perm), ConditionContext.forPermanent(perm, playerId),
                        entry -> {
                            if (referencedPermanent != null) {
                                entry.setTriggeringPermanentId(referencedPermanent.getId());
                                entry.setTriggeringPermanentControllerId(playerIdOf(
                                        gameData, referencedPermanent.getId()));
                                entry.setTriggeringCardId(referencedPermanent.getCard().getId());
                                entry.setDamageSourceCard(referencedPermanent.getCard());
                            }
                            entry.setStateTriggerEffectIndex(effectIndex);
                            entry.setSourcePermanentSnapshot(new Permanent(perm));
                            stamped[0] = entry;
                        }));
                if (!triggered) {
                    continue;
                }
                gameData.stateTriggerOnStack.add(key);
                parked |= stamped[0] != null && stamped[0].isParkedAtStackDoor();
                gameLogService.append(gameData, GameLog.text(description(perm) + " triggers."));
                log.info("Game {} - State trigger fires for {} (permanent {})",
                        gameData.id, perm.getCard().getName(), perm.getId());
            }
        }
        // A target chosen as the ability goes on the stack (CR 603.3d) is chosen now, as the game state triggered it.
        if (parked && triggerCollectionService != null) {
            triggerCollectionService.beginStackDoorChoices(gameData);
        }
    }

    /**
     * Where {@link #checkStateTriggers}' walk over one battlefield snapshot stands: the permanent it is at, that
     * permanent's state-triggered abilities as read when the walk reached it ({@code null} until then), and the next
     * ability to check.
     */
    private static final class StateTriggerCursor {
        private int permanentIndex;
        private List<TriggeredAbility> abilities;
        private int abilityIndex;
    }

    /** A state-triggered ability whose state holds: the permanent, the ability, its index and the permanent it names. */
    private record StateTriggerFinding(Permanent permanent, TriggeredAbility ability, int index,
                                       Permanent referencedPermanent) {}

    /**
     * Advances {@code cursor} to the next state-triggered ability of {@code snapshot} that triggers, or returns
     * {@code null} at its end. Changes nothing, so the walk judges abilities in one query scope until one triggers,
     * and closes it before putting that ability on the stack: each later ability is judged against the state the
     * stack door left, as when every ability was judged on its own.
     */
    private StateTriggerFinding nextStateTrigger(GameData gameData, List<Permanent> snapshot, UUID playerId,
                                                 StateTriggerCursor cursor) {
        while (cursor.permanentIndex < snapshot.size()) {
            Permanent perm = snapshot.get(cursor.permanentIndex);
            if (cursor.abilities == null) {
                cursor.abilities = perm.isFaceDown()
                        || gameQueryService != null && gameQueryService.hasLostPrintedAbilities(gameData, perm)
                        ? List.of() : stateTriggeredAbilities(gameData, perm);
                cursor.abilityIndex = 0;
            }
            while (cursor.abilityIndex < cursor.abilities.size()) {
                int i = cursor.abilityIndex++;
                TriggeredAbility ability = cursor.abilities.get(i);
                StateTriggerEffect trigger = MiscAbilityReader.stateTrigger(ability);

                // Rule 603.8: don't retrigger while already on the stack
                if (gameData.stateTriggerOnStack.contains(new StateTriggerKey(perm.getId(), i))) continue;
                if (!conditionMet(gameData, trigger, perm, playerId)) continue;
                Permanent referencedPermanent = findReferencedPermanent(
                        gameData, trigger.referencedPermanentPredicate(), perm, playerId);
                if (trigger.referencedPermanentPredicate() != null && referencedPermanent == null) {
                    continue;
                }
                return new StateTriggerFinding(perm, ability, i, referencedPermanent);
            }
            cursor.permanentIndex++;
            cursor.abilities = null;
        }
        return null;
    }

    private <T> T inQueryScope(GameData gameData, java.util.function.Supplier<T> queries) {
        return gameQueryService == null ? queries.get() : gameQueryService.withQueryScope(gameData, queries);
    }

    /** The stack and log description of a permanent's state-triggered ability. */
    private static String description(Permanent permanent) {
        return permanent.getCard().getName() + "'s state-triggered ability";
    }

    /**
     * The counter-threshold state triggers of the face-up planes: each goes to the stack door whole, its source the
     * plane and its controller the planar controller.
     */
    private void checkPlanarStateTriggers(GameData gameData) {
        if (gameData.planechase == null) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject source : List.copyOf(gameData.planechase.faceUp)) {
            List<TriggeredAbility> abilities = MiscAbilityReader.printed(source.getCard(), MiscWatch.STATE_TRIGGERED);
            if (abilities.isEmpty()) {
                continue;
            }
            Permanent counterSnapshot = new Permanent(source.getCard());
            counterSnapshot.getCounters().putAll(source.getCounters());
            for (int i = 0; i < abilities.size(); i++) {
                StateTriggerKey key = new StateTriggerKey(source.getId(), i);
                if (gameData.stateTriggerOnStack.contains(key) || !conditionMet(gameData,
                        MiscAbilityReader.stateTrigger(abilities.get(i)), counterSnapshot, planarControllerId)) {
                    continue;
                }
                int effectIndex = i;
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                        abilities.get(i), source.getCard(), null, planarControllerId,
                        source.getCard().getName() + "'s state trigger",
                        ConditionContext.forPermanent(counterSnapshot, planarControllerId), entry -> {
                            entry.setSourcePlanarObject(source.copy());
                            entry.setStateTriggerEffectIndex(effectIndex);
                        }));
                if (triggered) {
                    gameData.stateTriggerOnStack.add(key);
                    gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s state ability triggers."));
                }
            }
        }
    }

    /**
     * The state-triggered abilities of {@code permanent}: its printed ones, then the ones a static ability grants it
     * (Olivia, Crimson Bride's "it gains 'When you don't control a legendary Vampire, exile this creature'"), as
     * abilities layer 6 leaves it (CR 613.1f).
     */
    private List<TriggeredAbility> stateTriggeredAbilities(GameData gameData, Permanent permanent) {
        List<TriggeredAbility> abilities = new ArrayList<>(
                MiscAbilityReader.printed(permanent.getCard(), MiscWatch.STATE_TRIGGERED));
        if (gameQueryService != null) {
            for (CardEffect granted : gameQueryService.getGrantedEffects(gameData, permanent)) {
                if (granted instanceof GrantTriggeredAbilityEffect grant
                        && grant.grantedAbility() instanceof TriggeredAbility ability
                        && MiscAbilityReader.hasStateTrigger(ability)) {
                    abilities.add(ability);
                }
            }
        }
        return abilities;
    }

    private Permanent findReferencedPermanent(GameData gameData,
                                              PermanentPredicate predicate,
                                              Permanent sourcePermanent, UUID controllerId) {
        if (predicate == null) {
            return null;
        }
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(sourcePermanent.getCard().getId())
                .withSourceControllerId(controllerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, predicate, context)) {
                    return permanent;
                }
            }
        }
        return null;
    }

    private UUID playerIdOf(GameData gameData, UUID permanentId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.stream().anyMatch(p -> p.getId().equals(permanentId))) {
                return playerId;
            }
        }
        return null;
    }

    /**
     * Removes tracking for a resolved/countered state-triggered ability.
     * Must be called whenever a stack entry that originated from a state trigger
     * leaves the stack (resolution, counter, or fizzle).
     */
    public void cleanupResolvedStateTrigger(GameData gameData, StackEntry entry) {
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getStateTriggerEffectIndex() >= 0) {
            UUID sourceId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId()
                    : entry.getSourcePlanarObject() != null ? entry.getSourcePlanarObject().getId() : null;
            if (sourceId == null) {
                return;
            }
            gameData.stateTriggerOnStack.remove(
                    new StateTriggerKey(sourceId, entry.getStateTriggerEffectIndex()));
        }
    }
}
