package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "235")
public class RavensRunDragoon extends Card {

    public RavensRunDragoon() {
        // "This creature can't be blocked by black creatures."
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK))));
    }
}
