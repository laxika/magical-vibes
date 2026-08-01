package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that discards the controller's entire hand. Used as an activated-ability cost
 * (Slate of Ancestry) and as an additional cast cost in the SPELL slot (Kaervek's Spite). Unlike
 * {@link DiscardHandEffect}, this is paid at activation/cast time (before resolution). There is
 * no card choice and no legality restriction — an empty hand can always be discarded. Fires
 * per-card discard triggers. For SPELL casts the spell has already left the hand (CR 601.2a), so
 * it is never discarded as its own cost.
 */
public record DiscardHandCost() implements CostEffect {
}
