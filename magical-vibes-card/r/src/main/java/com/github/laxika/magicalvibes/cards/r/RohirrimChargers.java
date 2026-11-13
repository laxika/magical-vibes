package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "496")
@CardRegistration(set = "LTC", collectorNumber = "540")
public class RohirrimChargers extends Card {

    public RohirrimChargers() {
        addStaticEffect(new ExertAsItAttacksEffect());

        addTrigger(Triggers.youExertCreature(),
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT));
    }
}
