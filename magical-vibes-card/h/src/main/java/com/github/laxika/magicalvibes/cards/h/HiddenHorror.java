package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "149")
@CardRegistration(set = "P02", collectorNumber = "75")
@CardRegistration(set = "6ED", collectorNumber = "137")
@CardRegistration(set = "WTH", collectorNumber = "72")
@CardRegistration(set = "PD3", collectorNumber = "2")
public class HiddenHorror extends Card {

    public HiddenHorror() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(DiscardEffect.matching(1, Subjects.you(), new CardTypePredicate(CardType.CREATURE)),
                "Discard a creature card? If you don't, sacrifice it.",
                new SacrificeSelfEffect()));
    }
}
