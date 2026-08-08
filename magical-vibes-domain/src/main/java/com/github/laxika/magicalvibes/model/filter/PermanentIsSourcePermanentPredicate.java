package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches the ability's own source permanent, identified by permanent id rather than card id, so a
 * second copy of the same card on the battlefield is not matched. Wrap in {@link PermanentNotPredicate}
 * for the "each <b>other</b> …" wording. Requires the evaluating call site to supply the source via
 * {@link FilterContext#withSourcePermanentSnapshot(com.github.laxika.magicalvibes.model.Permanent)}.
 */
public record PermanentIsSourcePermanentPredicate() implements PermanentPredicate {
}
