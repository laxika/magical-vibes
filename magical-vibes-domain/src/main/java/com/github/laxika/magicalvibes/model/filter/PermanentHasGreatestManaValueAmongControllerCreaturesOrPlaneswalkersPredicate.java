package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures or planeswalkers tied for greatest mana value among the permanents
 * controlled by the candidate permanent's controller.
 */
public record PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate()
        implements PermanentPredicate {
}
