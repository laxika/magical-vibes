package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at the top cards of the controller's library, exile one face down tracked with the
 * source permanent, and put the rest on the bottom of that library in any order, or randomly when
 * {@code randomOrder} is true. When {@code castPermissionFilter} is non-null the controller may
 * cast the exiled card for as long as it remains exiled, provided it matches the filter; this
 * permission does not depend on the source permanent remaining on the battlefield.
 */
public record ExileOneFromTopCardsFaceDownWithSourceEffect(int count, boolean randomOrder,
                                                           CardPredicate castPermissionFilter) implements CardEffect {

    public ExileOneFromTopCardsFaceDownWithSourceEffect(int count) {
        this(count, false, null);
    }

    public ExileOneFromTopCardsFaceDownWithSourceEffect(int count, boolean randomOrder) {
        this(count, randomOrder, null);
    }
}
