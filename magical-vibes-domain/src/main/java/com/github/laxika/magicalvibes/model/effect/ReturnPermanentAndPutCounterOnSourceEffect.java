package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns one matching permanent to its owner's hand and then puts a +1/+1 counter on the source
 * permanent. If no matching permanent exists, the handler can resolve the card's alternative branch.
 * The return and counter placement are one resolution step for "if you do" wording.
 */
public record ReturnPermanentAndPutCounterOnSourceEffect(
        PermanentPredicate filter,
        String permanentDescription
) implements CardEffect {
}
