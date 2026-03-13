package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this creature enters, put a phylactery counter on an artifact you control."
 * Handled as a replacement effect (not a triggered ability) — the counter is placed
 * as part of the entering process, before state-based actions are checked.
 *
 * <p>Per MTG rulings, this does NOT target the artifact (shroud/hexproof don't
 * prevent it). The artifact is chosen as the creature enters, not when the spell
 * is cast. If the controller has no artifacts, the ability does nothing.</p>
 */
public record PutPhylacteryCounterOnTargetPermanentEffect() implements ReplacementEffect {
}
