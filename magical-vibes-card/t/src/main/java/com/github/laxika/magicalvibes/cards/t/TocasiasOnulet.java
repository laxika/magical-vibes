package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "39")
public class TocasiasOnulet extends Card {

    public TocasiasOnulet() {
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new GainLifeEffect(2));
        addUnearth("{3}{W}");
    }
}
