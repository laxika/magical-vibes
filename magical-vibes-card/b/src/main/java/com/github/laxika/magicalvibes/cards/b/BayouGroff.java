package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrPayManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "STX", collectorNumber = "121")
public class BayouGroff extends Card {

    public BayouGroff() {
        // As an additional cost to cast this spell, sacrifice a creature or pay {3}.
        addCastingCost(new SacrificePermanentOrPayManaCost("{3}", new PermanentIsCreaturePredicate(), "a creature"));
    }
}
