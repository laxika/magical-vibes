package com.github.laxika.magicalvibes.service.state;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StateTriggerKey;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implements MTG rule 603.8 — state-triggered abilities.
 *
 * <p>After state-based actions run, this service checks every permanent for
 * {@link EffectSlot#STATE_TRIGGERED} effects whose condition is met. If the
 * ability is not already on the stack (tracked via
 * {@link GameData#stateTriggerOnStack}), a triggered ability is pushed onto
 * the stack. The trigger won't fire again until it resolves, is countered,
 * or otherwise leaves the stack.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StateTriggerService {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * Evaluates a state trigger's condition: a {@code sourcePredicate} goes through the
     * layer-aware predicate evaluator (so static keyword/color/type grants count), otherwise the
     * free-form {@link com.github.laxika.magicalvibes.model.effect.StateTriggerPredicate} runs.
     */
    private boolean conditionMet(GameData gameData, StateTriggerEffect trigger, Permanent perm, UUID controllerId) {
        if (trigger.sourcePredicate() != null) {
            return predicateEvaluationService.matchesPermanentPredicate(perm, trigger.sourcePredicate(),
                    FilterContext.of(gameData)
                            .withSourceCardId(perm.getCard().getId())
                            .withSourceControllerId(controllerId));
        }
        return trigger.predicate().test(gameData, perm, controllerId);
    }

    /**
     * Checks all permanents for state-triggered abilities whose conditions are met.
     * For each qualifying permanent not already tracked, pushes a triggered ability
     * onto the stack and marks it in {@link GameData#stateTriggerOnStack}.
     *
     * <p>Iterates in APNAP order (orderedPlayerIds) so triggers are stacked correctly.</p>
     */
    public void checkStateTriggers(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            // Snapshot to avoid ConcurrentModificationException if a trigger modifies the list
            List<Permanent> snapshot = List.copyOf(battlefield);
            for (Permanent perm : snapshot) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.STATE_TRIGGERED);
                for (int i = 0; i < effects.size(); i++) {
                    StateTriggerEffect trigger = (StateTriggerEffect) effects.get(i);

                    // Rule 603.8: don't retrigger while already on the stack
                    StateTriggerKey key = new StateTriggerKey(perm.getId(), i);
                    if (gameData.stateTriggerOnStack.contains(key)) continue;

                    if (conditionMet(gameData, trigger, perm, playerId)) {
                        Permanent referencedPermanent = findReferencedPermanent(
                                gameData, trigger.referencedPermanentPredicate(), perm, playerId);
                        if (trigger.referencedPermanentPredicate() != null && referencedPermanent == null) {
                            continue;
                        }
                        gameData.stateTriggerOnStack.add(key);

                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                trigger.description(),
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
                        gameData.stack.add(entry);

                        gameLogService.append(gameData, GameLog.text(trigger.description() + " triggers."));
                        log.info("Game {} - State trigger fires for {} (permanent {})",
                                gameData.id, perm.getCard().getName(), perm.getId());
                    }
                }
            }
        }
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
                && entry.getSourcePermanentId() != null
                && entry.getStateTriggerEffectIndex() >= 0) {
            gameData.stateTriggerOnStack.remove(
                    new StateTriggerKey(entry.getSourcePermanentId(), entry.getStateTriggerEffectIndex()));
        }
    }
}
