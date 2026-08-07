package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One active "whenever you gain life this turn, each opponent loses that much life" delayed
 * triggered ability (Vizkopa Guildmage's second ability).
 *
 * <p>Registered when the activated ability resolves and cleared at turn cleanup. Each activation
 * adds its own watcher, so activating twice makes a single life-gain event drain twice. The watcher
 * outlives its source permanent — once the ability has resolved, the delayed trigger keeps firing
 * for the rest of the turn even if the source leaves the battlefield.
 *
 * @param controllerId the player who activated the ability; only their life gain triggers it, and
 *                     "each opponent" is relative to them
 * @param sourceCard   the source card, kept for the trigger's stack entry and log text
 */
public record LifeGainOpponentLifeLossWatcher(UUID controllerId, Card sourceCard) {
}
