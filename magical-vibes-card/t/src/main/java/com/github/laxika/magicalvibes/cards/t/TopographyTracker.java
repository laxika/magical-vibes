package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleExploreReplacementEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "63")
@CardRegistration(set = "LCC", collectorNumber = "95")
public class TopographyTracker extends Card {

    public TopographyTracker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofMapToken(1));
        addStaticEffect(new DoubleExploreReplacementEffect());
    }
}
