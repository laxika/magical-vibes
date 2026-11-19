package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M11", collectorNumber = "104")
@CardRegistration(set = "PC2", collectorNumber = "34")
@CardRegistration(set = "PCA", collectorNumber = "34")
public class LilianasSpecter extends Card {

    public LilianasSpecter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardEffect(1, Subjects.eachOpponent()));
    }
}
