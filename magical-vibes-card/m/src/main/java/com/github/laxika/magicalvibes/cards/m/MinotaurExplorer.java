package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "206")
public class MinotaurExplorer extends Card {

    public MinotaurExplorer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(new DiscardEffect(1, Subjects.you(), true),
                "Discard a card at random? If you don't, sacrifice it.",
                new SacrificeSelfEffect()));
    }
}
