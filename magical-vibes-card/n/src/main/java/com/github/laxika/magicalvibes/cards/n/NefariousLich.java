package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "153")
public class NefariousLich extends Card {

    public NefariousLich() {
        addStaticEffect(new NefariousLichDamageReplacementEffect());
        addStaticEffect(new NefariousLichLifeGainReplacementEffect());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ControllerLosesGameOnLeavesEffect());
    }
}
