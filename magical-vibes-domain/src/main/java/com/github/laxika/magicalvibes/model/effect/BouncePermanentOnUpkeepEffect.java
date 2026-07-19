package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.Set;

/**
 * At the beginning of an upkeep, a player returns a permanent they control matching
 * {@code filters} to its owner's hand. The choosing player is either the source's
 * controller ({@link Scope#SOURCE_CONTROLLER}, e.g. Stampeding Wildebeests, Esperzoa) or
 * the player whose upkeep it is ({@link Scope#TRIGGER_TARGET_PLAYER}, e.g. Sunken Hope).
 *
 * <p>The permanent type is expressed entirely through {@code filters} (a creature predicate
 * for creature-only cards, an artifact predicate for Esperzoa, …); the resolver only iterates
 * the choosing player's battlefield and applies these filters — it does not hardcode a type.
 * Empty filters would therefore match every permanent, so always supply a type predicate.
 * If the player controls no matching permanent nothing happens; otherwise they choose which
 * one via the shared {@code BounceCreature} choice context.</p>
 */
public record BouncePermanentOnUpkeepEffect(
        Scope scope,
        Set<TargetFilter> filters,
        String prompt
) implements CardEffect {

    public enum Scope {
        SOURCE_CONTROLLER,
        TRIGGER_TARGET_PLAYER
    }
}
