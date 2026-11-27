package com.github.laxika.magicalvibes.model.filter;

/**
 * A Ring-bearer (CR 701.54a): the permanent its controller last chose as their Ring-bearer. The designation ends when
 * another player gains control of it (CR 701.54a), so a Ring-bearer is always its controller's; "your Ring-bearer" is
 * this under a "you control" relation or with {@link PermanentControlledBySourceControllerPredicate} (CR 701.54e).
 */
public record PermanentIsRingBearerPredicate() implements PermanentPredicate {
}
