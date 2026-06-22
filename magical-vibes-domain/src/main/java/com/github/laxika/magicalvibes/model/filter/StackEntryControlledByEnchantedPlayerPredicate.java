package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches stack entries controlled by the player that the evaluating source permanent
 * is attached to (the "enchanted player").
 * <p>
 * Unlike {@link StackEntryControlledByPredicate} — which matches entries controlled by the
 * source's own controller — this predicate references the source aura's {@code attachedTo}.
 * The enchanted player's identity is supplied externally by the evaluating service; this
 * record is a marker only.
 * <p>
 * Used by Curse of Echoes ("whenever enchanted player casts an instant or sorcery spell …").
 */
public record StackEntryControlledByEnchantedPlayerPredicate() implements StackEntryPredicate {
}
