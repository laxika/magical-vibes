package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/** Common capability for permissions that let a player play or cast a card from a graveyard. */
public interface GraveyardPlayPermission extends CardEffect {

    /** True if the permission can be used only once during each controller turn. */
    default boolean oncePerControllerTurn() {
        return false;
    }

    /** True if the permission can be used once for a land during each controller turn. */
    default boolean oncePerControllerTurnForLand() {
        return false;
    }

    /** True if the permission can be used once for a spell during each controller turn. */
    default boolean oncePerControllerTurnForSpell() {
        return false;
    }

    /** Which graveyards the permission can use, relative to its controller. */
    default GraveyardSearchScope graveyardScope() {
        return GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
    }

    /** True if cards must have entered a graveyard from a library during the current turn. */
    default boolean onlyCardsPutIntoGraveyardFromLibraryThisTurn() {
        return false;
    }

    /** True if the permission can be used only during its controller's turn. */
    default boolean onlyDuringControllerTurn() {
        return false;
    }

    /** Triggered ability granted to a permanent that enters through this permission. */
    default GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant() {
        return null;
    }
}
