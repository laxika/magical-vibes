package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents currently attached to the source permanent.
 *
 * <p>The source is supplied through the evaluation context, which lets targeted triggered
 * abilities restrict a target to an attachment of the permanent that caused the trigger.</p>
 */
public record PermanentAttachedToSourcePermanentPredicate() implements PermanentPredicate {
}
