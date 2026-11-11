package com.github.laxika.magicalvibes.service.state;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StateTriggerKey;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.MiscAbilityReader;
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
 * <p>After state-based actions run, this service checks every permanent for
 * state-triggered abilities ({@code Triggers.state()}, read by {@link MiscAbilityReader#stateTriggers} as one
 * {@link StateTriggerEffect} of each ability's game state) whose condition is met. If the
 * ability is not already on the stack (tracked via
 * {@link GameData#stateTriggerOnStack}), a triggered ability is pushed onto
 * the stack. The trigger won't fire again until it resolves, is countered,
 * or otherwise leaves the stack.</p>
 */
@Slf4j
@Service
public class StateTriggerService {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final ConditionEvaluationService conditionEvaluationService;

    @Autowired
    public StateTriggerService(GameLogService gameLogService,
                               PredicateEvaluationService predicateEvaluationService,
                               GameQueryService gameQueryService,
                               @Lazy ETBTokenTargetService etbTokenTargetService,
                               @Lazy ConditionEvaluationService conditionEvaluationService) {
        this.gameLogService = gameLogService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameQueryService = gameQueryService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    public StateTriggerService(GameLogService gameLogService,
                               PredicateEvaluationService predicateEvaluationService,
                               GameQueryService gameQueryService) {
        this(gameLogService, predicateEvaluationService, gameQueryService, null, null);
    }

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
     * Whether the intervening "if" of a state trigger holds as its game state triggers it (CR 603.4): the reader
     * ({@link MiscAbilityReader#stateTriggers}) wraps each of the ability's effects in it, as a
     * {@link ConditionalEffect} that is checked again as the ability resolves, and a state trigger's effects have no
     * conditions of their own, so every conditional among them is that "if". Without a condition evaluator (the
     * unit-test constructors) nothing is checked here.
     */
    private boolean interveningIfMet(GameData gameData, StateTriggerEffect trigger, Permanent perm,
                                     UUID controllerId) {
        if (conditionEvaluationService == null) {
            return true;
        }
        for (CardEffect effect : trigger.effects()) {
            if (effect instanceof ConditionalEffect conditional
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(perm, controllerId))) {
                return false;
            }
        }
        return true;
    }

    /** The stack and log description of a state trigger: its own, or "[card name]'s state-triggered ability". */
    private static String description(StateTriggerEffect trigger, Card card) {
        return trigger.description() != null ? trigger.description() : card.getName() + "'s state-triggered ability";
    }

    /**
     * Checks all permanents for state-triggered abilities whose conditions are met.
     * For each qualifying permanent not already tracked, pushes a triggered ability
     * onto the stack and marks it in {@link GameData#stateTriggerOnStack}.
     *
     * <p>Iterates in APNAP order (orderedPlayerIds) so triggers are stacked correctly.
     * Nothing triggers once the game has ended.</p>
     */
    public void checkStateTriggers(GameData gameData) {
        if (gameData.status == GameStatus.FINISHED) {
            return;
        }
        checkPlanarStateTriggers(gameData);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            // Snapshot to avoid ConcurrentModificationException if a trigger modifies the list
            List<Permanent> snapshot = List.copyOf(battlefield);
            for (Permanent perm : snapshot) {
                if (perm.isFaceDown()) continue;
                if (gameQueryService != null && gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                List<StateTriggerEffect> effects = stateTriggeredEffects(gameData, perm);
                for (int i = 0; i < effects.size(); i++) {
                    StateTriggerEffect trigger = effects.get(i);

                    // Rule 603.8: don't retrigger while already on the stack
                    StateTriggerKey key = new StateTriggerKey(perm.getId(), i);
                    if (gameData.stateTriggerOnStack.contains(key)) continue;

                    if (conditionMet(gameData, trigger, perm, playerId)
                            && interveningIfMet(gameData, trigger, perm, playerId)) {
                        Permanent referencedPermanent = findReferencedPermanent(
                                gameData, trigger.referencedPermanentPredicate(), perm, playerId);
                        if (trigger.referencedPermanentPredicate() != null && referencedPermanent == null) {
                            continue;
                        }
                        gameData.stateTriggerOnStack.add(key);

                        // The entry of the ability that chooses nothing is also the record a target choice is given.
                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                description(trigger, perm.getCard()),
                                trigger.effects(),
                                null,
                                perm.getId()
                        );
                        if (referencedPermanent != null) {
                            entry.setTriggeringPermanentId(referencedPermanent.getId());
                            entry.setTriggeringPermanentControllerId(playerIdOf(
                                    gameData, referencedPermanent.getId()));
                            entry.setTriggeringCardId(referencedPermanent.getCard().getId());
                            entry.setDamageSourceCard(referencedPermanent.getCard());
                        }
                        entry.setStateTriggerEffectIndex(i);
                        gameData.handOffTriggerRecord(entry);
                        if (hasTargets(perm, trigger)) {
                            PermanentChoiceContext.ETBTokenMultiTargetTrigger pending =
                                    new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                            perm.getCard(), playerId, trigger.effects(), perm.getId(),
                                            List.of(), 0, 0)
                                            .withStateTriggerEffectIndex(i);
                            gameData.queueInteraction(pending);
                            if (!gameData.interaction.isAwaitingInput()) {
                                etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
                            }
                        } else {
                            gameData.stack.add(entry);
                        }

                        gameLogService.append(gameData,
                                GameLog.text(description(trigger, perm.getCard()) + " triggers."));
                        log.info("Game {} - State trigger fires for {} (permanent {})",
                                gameData.id, perm.getCard().getName(), perm.getId());
                    }
                }
            }
        }
    }

    /** Queues counter-threshold state triggers from the currently face-up planes. */
    private void checkPlanarStateTriggers(GameData gameData) {
        if (gameData.planechase == null) {
            return;
        }
        for (var source : gameData.planechase.faceUp) {
            List<StateTriggerEffect> effects = MiscAbilityReader.stateTriggers(source.getCard());
            if (effects.isEmpty()) {
                continue;
            }
            Permanent counterSnapshot = new Permanent(source.getCard());
            counterSnapshot.getCounters().putAll(source.getCounters());
            for (int i = 0; i < effects.size(); i++) {
                StateTriggerEffect trigger = effects.get(i);
                if (!conditionMet(gameData, trigger, counterSnapshot, gameData.planechase.controllerId)
                        || !interveningIfMet(gameData, trigger, counterSnapshot, gameData.planechase.controllerId)) {
                    continue;
                }
                StateTriggerKey key = new StateTriggerKey(source.getId(), i);
                if (!gameData.stateTriggerOnStack.add(key)) {
                    continue;
                }
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(),
                        gameData.planechase.controllerId, source.getCard().getName() + "'s state trigger",
                        new ArrayList<>(trigger.effects()), 0, (UUID) null);
                entry.setSourcePlanarObject(source.copy());
                entry.setStateTriggerEffectIndex(i);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s state ability triggers."));
            }
        }
    }

    private boolean hasTargets(Permanent permanent, StateTriggerEffect trigger) {
        if (etbTokenTargetService == null || permanent.getCard().getTargetGroups().isEmpty()) {
            return false;
        }
        return trigger.effects().stream().anyMatch(effect ->
                effect.targetSpec().admits(com.github.laxika.magicalvibes.model.effect.TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(com.github.laxika.magicalvibes.model.effect.TargetPredicate.Kind.PLAYER));
    }

    /**
     * The state triggers of {@code permanent}: its printed ones, read from its abilities, then the state-triggered
     * abilities a static ability grants it (Olivia, Crimson Bride's "it gains 'When you don't control a legendary
     * Vampire, exile this creature'"), as abilities layer 6 leaves it (CR 613.1f).
     */
    private List<StateTriggerEffect> stateTriggeredEffects(GameData gameData, Permanent permanent) {
        List<StateTriggerEffect> effects = new ArrayList<>(MiscAbilityReader.stateTriggers(permanent.getCard()));
        if (gameQueryService != null) {
            for (CardEffect granted : gameQueryService.getGrantedEffects(gameData, permanent)) {
                if (granted instanceof GrantTriggeredAbilityEffect grant
                        && grant.grantedAbility() instanceof TriggeredAbility ability
                        && MiscAbilityReader.hasStateTrigger(ability)) {
                    effects.add(MiscAbilityReader.stateTrigger(ability));
                }
            }
        }
        return effects;
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
