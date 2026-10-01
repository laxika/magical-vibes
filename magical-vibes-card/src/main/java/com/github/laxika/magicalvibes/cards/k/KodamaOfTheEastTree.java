package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "BLC", collectorNumber = "227")
public class KodamaOfTheEastTree extends Card {

    public KodamaOfTheEastTree() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent")
                                .boundedByEventValue()
                                .withTriggeringPermanentEntrySuppressed(),
                        "Put a permanent card with equal or lesser mana value from your hand onto the battlefield?"));
    }
}
