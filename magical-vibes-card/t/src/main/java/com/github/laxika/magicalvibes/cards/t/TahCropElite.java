package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "31")
@CardRegistration(set = "AKR", collectorNumber = "40")
public class TahCropElite extends Card {

    public TahCropElite() {
        addStaticEffect(new ExertAsItAttacksEffect());
        addTrigger(Triggers.exertedAsItAttacks(), new BoostAllOwnCreaturesEffect(1, 1));
    }
}
