package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "141")
public class TotemSpeaker extends Card {

    public TotemSpeaker() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.BEAST),
                        new MayEffect(new GainLifeEffect(3), "Gain 3 life?")));
    }
}
