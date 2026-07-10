package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that discards the controller's entire hand as part of an activated ability's cost
 * (e.g. Slate of Ancestry). Unlike {@link DiscardHandEffect}, this is paid during activation
 * (before resolution). There is no card choice and no legality restriction — an empty hand can
 * always be discarded. Fires per-card discard triggers.
 */
public record DiscardHandCost() implements CostEffect {
}
