package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "616")
public class AtlanteanSkirmisher extends Card {

    public AtlanteanSkirmisher() {
        addTrigger(Triggers.attacks(), new DrawDiscardAndConniveEffect());
    }
}
