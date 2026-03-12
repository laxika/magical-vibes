package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
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

    private final GameBroadcastService gameBroadcastService;

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
                for (var effect : perm.getCard().getEffects(EffectSlot.STATE_TRIGGERED)) {
                    if (!(effect instanceof StateTriggerEffect trigger)) continue;

                    // Rule 603.8: don't retrigger while already on the stack
                    if (gameData.stateTriggerOnStack.contains(perm.getId())) continue;

                    if (trigger.predicate().test(gameData, perm, playerId)) {
                        gameData.stateTriggerOnStack.add(perm.getId());

                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                trigger.description(),
                                trigger.effects(),
                                null,
                                perm.getId()
                        );
                        gameData.stack.add(entry);

                        gameBroadcastService.logAndBroadcast(gameData,
                                trigger.description() + " triggers.");
                        log.info("Game {} - State trigger fires for {} (permanent {})",
                                gameData.id, perm.getCard().getName(), perm.getId());
                    }
                }
            }
        }
    }

    /**
     * Removes tracking for a resolved/countered state-triggered ability.
     * Must be called whenever a stack entry that originated from a state trigger
     * leaves the stack (resolution, counter, or fizzle).
     */
    public void cleanupResolvedStateTrigger(GameData gameData, StackEntry entry) {
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getSourcePermanentId() != null) {
            gameData.stateTriggerOnStack.remove(entry.getSourcePermanentId());
        }
    }
}
