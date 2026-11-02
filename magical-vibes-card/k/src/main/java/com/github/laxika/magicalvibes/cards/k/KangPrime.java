package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandAndSuspendEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "85")
@CardRegistration(set = "MSC", collectorNumber = "406")
public class KangPrime extends Card {

    public KangPrime() {
        ExileTopUntilNonlandAndSuspendEffect effect = new ExileTopUntilNonlandAndSuspendEffect(2);
        addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        addEffect(EffectSlot.ON_ATTACK, effect);
    }
}
