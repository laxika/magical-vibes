package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "115")
public class WarteyeWitch extends Card {

    public WarteyeWitch() {
        ScryEffect deathEffect = new ScryEffect(1);
        addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL), deathEffect);
    }
}
