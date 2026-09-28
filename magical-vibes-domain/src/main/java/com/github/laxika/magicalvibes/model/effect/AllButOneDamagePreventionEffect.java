package com.github.laxika.magicalvibes.model.effect;

/** Capability for static effects that reduce qualifying damage events to at most one. */
public interface AllButOneDamagePreventionEffect extends CardEffect {

    /** Whether the effect protects its controller from damage. */
    default boolean protectsController() {
        return true;
    }

    /** Whether the effect protects planeswalkers controlled by its controller. */
    default boolean protectsPlaneswalkers() {
        return false;
    }

    /** Whether the effect protects Heroes controlled by its controller. */
    default boolean protectsHeroes() {
        return false;
    }
}
