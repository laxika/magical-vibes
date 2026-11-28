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

    /**
     * Whether this is hideaway (CR 702.75a), the form that puts the rest on the bottom in a random order: each later
     * controller of the source may look at the exiled card too (Howltooth Hollow's 2022-04-29 ruling), unlike a
     * face-down exile such as Kayla's Music Box's.
     */
    public boolean hideaway() {
        return randomOrder;
    }
}
