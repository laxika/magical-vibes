package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "187")
public class VineshaperProdigy extends Card {

    public VineshaperProdigy() {
        addStaticEffect(new KickerEffect("{1}{U}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new Kicked(),
                        LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3))));
    }
}
