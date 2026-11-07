package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that was put onto the battlefield with an ability of the filter's source permanent
 * ({@code PutCardToBattlefieldEffect} records it on the entered permanent). Wrap in {@link PermanentNotPredicate} for
 * "if it wasn't put onto the battlefield with this ability" (Kodama of the East Tree), a fact of the entering
 * permanent that can't change, so a trigger filter says it.
 */
public record PermanentPutOntoBattlefieldBySourcePredicate() implements PermanentPredicate {
}
