package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power equals their effective toughness. Wrap in
 * {@link PermanentNotPredicate} for "whose power and toughness aren't equal" (Gilt-Leaf Winnower).
 */
public record PermanentPowerEqualsToughnessPredicate() implements PermanentPredicate {
}
