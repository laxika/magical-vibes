package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "304")
@CardRegistration(set = "DDD", collectorNumber = "6")
@CardRegistration(set = "GVL", collectorNumber = "6")
public class WirewoodSavage extends Card {

    public WirewoodSavage() {
        addTrigger(Triggers.enters(null, ObjectRelation.ANY),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.BEAST),
                        new MayEffect(new DrawCardEffect(1), "Draw a card?")));
    }
}
