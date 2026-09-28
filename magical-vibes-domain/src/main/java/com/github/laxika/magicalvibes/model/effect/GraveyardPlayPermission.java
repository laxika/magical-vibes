package com.github.laxika.magicalvibes.model.effect;

/** Common capability for permissions that let a player play or cast a card from a graveyard. */
public interface GraveyardPlayPermission extends CardEffect {

    /** True if the permission can be used only once during each controller turn. */
    default boolean oncePerControllerTurn() {
        return false;
    }

    /** Whether a permanent entering through this permission is exiled if it would leave. */
    default boolean exileIfLeavesBattlefield() {
        return false;
    }

    /** Triggered ability granted to a permanent that enters through this permission. */
    default GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant() {
        return null;
    }
}
