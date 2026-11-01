package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "318")
@CardRegistration(set = "THB", collectorNumber = "297")
public class TreeshakerChimera extends Card {

    public TreeshakerChimera() {
        addStaticEffect(new MustBeBlockedByAllCreaturesEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect(3));
    }
}
