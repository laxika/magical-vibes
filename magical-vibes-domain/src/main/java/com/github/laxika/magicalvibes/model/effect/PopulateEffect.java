package com.github.laxika.magicalvibes.model.effect;

/**
 * "Populate" — choose a creature token you control, then create a token that's a copy of that
 * creature token (CR 701.36a).
 *
 * <p>Populate does not target: the choice is made by the controller as the spell or ability
 * resolves, so hexproof/shroud and "can't be the target of" effects never apply. With no creature
 * token the effect does nothing; with exactly one the choice is forced. The copy is made from all
 * copiable characteristics per CR 707.2 and respects the controller's token multiplier.
 *
 * <p>The bulk form ("for each creature token you control...") is
 * {@link CreateTokenCopyOfEachControlledCreatureTokenEffect}.
 */
public record PopulateEffect() implements CardEffect {
}
