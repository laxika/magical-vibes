package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a Vehicle the source crewed this turn: the source permanent was tapped to pay its crew cost (CR 702.122c),
 * "whenever a Vehicle crewed by Balthier and Fran this turn attacks". The inverse of
 * {@link PermanentCrewedBySourceThisTurnPredicate}, which matches the creatures that crewed the source.
 */
public record PermanentCrewedThisTurnBySourcePredicate() implements PermanentPredicate {
}
