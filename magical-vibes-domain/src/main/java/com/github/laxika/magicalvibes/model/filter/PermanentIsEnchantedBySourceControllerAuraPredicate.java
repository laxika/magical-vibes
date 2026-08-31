package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent with an attached Aura controlled by the source controller.
 * The host and the Aura may have different controllers.
 */
public record PermanentIsEnchantedBySourceControllerAuraPredicate() implements PermanentPredicate {
}
