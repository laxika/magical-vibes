package com.github.laxika.magicalvibes.carddata.store;

import java.util.List;

/**
 * How a printing looks, as opposed to what it is: what the card browser uses to tell a set's base
 * printings from its showcase, borderless and promo variants. Values use Scryfall's lower-case
 * vocabulary, which a source for another provider has to map into.
 *
 * @param promoTypes   e.g. "planeswalkerdeck", "setextension", "boosterfun"; empty for an ordinary
 *                     printing
 * @param frameEffects e.g. "showcase", "extendedart", "legendary"
 * @param borderColor  e.g. "black", "borderless"
 */
public record PrintVariant(
        List<String> promoTypes,
        List<String> frameEffects,
        String borderColor,
        boolean fullArt) {
}
