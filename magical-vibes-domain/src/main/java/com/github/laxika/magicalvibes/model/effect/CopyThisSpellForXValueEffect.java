package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for a spell that copies itself once for each point of its cast-time X value.
 *
 * <p>The self-cast trigger collector snapshots the spell and turns this marker into the ordinary
 * spell-copy resolution effect. A zero X value produces no trigger.</p>
 */
public record CopyThisSpellForXValueEffect() implements CardEffect {
}
