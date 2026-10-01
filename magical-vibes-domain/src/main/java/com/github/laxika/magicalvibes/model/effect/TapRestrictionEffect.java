package com.github.laxika.magicalvibes.model.effect;

/**
 * Describes a floating restriction on how a permanent may become tapped.
 *
 * <p>The interface exposes the rule fact needed by the permanent state and turn-expiry
 * machinery without coupling those services to a concrete card effect.</p>
 */
public interface TapRestrictionEffect extends CardEffect {

    /** Whether the affected permanent may become tapped only while it is attacking. */
    default boolean preventsTappingUnlessAttacking() {
        return false;
    }
}
