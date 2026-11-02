package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "585")
public class BlurOfHeroism extends Card {

    public BlurOfHeroism() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addStaticEffect(new GrantFlashToCardTypeEffect(new CardSubtypePredicate(CardSubtype.HERO)));
    }
}
