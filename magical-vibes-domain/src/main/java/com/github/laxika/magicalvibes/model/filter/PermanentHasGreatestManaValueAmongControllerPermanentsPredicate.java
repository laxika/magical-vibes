package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent tied for the greatest mana value among all the permanents its controller controls, lands
 * included ("a nonland permanent they control with the greatest mana value among permanents they control" is this
 * with a nonland filter, Dispersal). A permanent with {X} in its mana cost counts X as 0.
 */
public record PermanentHasGreatestManaValueAmongControllerPermanentsPredicate() implements PermanentPredicate {
}
