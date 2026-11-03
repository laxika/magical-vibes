package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents that entered the battlefield by resolving as a spell that was cast, rather than being put onto it
 * ("if it wasn't cast").
 */
public record PermanentWasCastPredicate() implements PermanentPredicate {
}
