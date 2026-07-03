package com.github.laxika.magicalvibes.model.filter;

/**
 * A predicate over a player. Predicates are pure data — evaluation lives in the engine's
 * targeting services, which dispatch over this sealed hierarchy.
 */
public sealed interface PlayerPredicate permits
        PlayerRelationPredicate {
}
