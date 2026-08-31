package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent enchanted by at least one Aura controlled by the static effect's source
 * controller.
 */
public record PermanentIsEnchantedByAuraControlledBySourceControllerPredicate()
        implements PermanentPredicate {
}
