package com.github.laxika.magicalvibes.model.effect;

public interface CardEffect {
    default boolean canTargetPlayer() { return false; }
    default boolean canTargetPermanent() { return false; }
    default boolean canTargetSpell() { return false; }
    default boolean canTargetGraveyard() { return false; }
    /** True if this effect triggers once per blocking creature (e.g. "becomes blocked by a creature"). */
    default boolean triggersPerBlocker() { return false; }
}
