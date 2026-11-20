package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "167")
@CardRegistration(set = "ALL", collectorNumber = "65")
@CardRegistration(set = "A25", collectorNumber = "120")
@CardRegistration(set = "BTD", collectorNumber = "34")
@CardRegistration(set = "DKM", collectorNumber = "10")
@CardRegistration(set = "ME1", collectorNumber = "86")
public class BalduvianHorde extends Card {

    public BalduvianHorde() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(new DiscardEffect(1, Subjects.you(), true),
                "Discard a card at random? If you don't, sacrifice it.",
                new SacrificeSelfEffect()));
    }
}
