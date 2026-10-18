package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for a spell that copies itself once for each point of its cast-time X value.
 *
 * <p>The self-cast trigger collector snapshots the spell and turns this marker into the ordinary
 * spell-copy resolution effect. The cast trigger still goes on the stack when X is zero; it then makes no copies.</p>
 */
public record CopyThisSpellForXValueEffect() implements CardEffect {
}
