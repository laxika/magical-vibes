package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "96")
@CardRegistration(set = "7ED", collectorNumber = "93")
@CardRegistration(set = "8ED", collectorNumber = "93")
@CardRegistration(set = "9ED", collectorNumber = "88")
@CardRegistration(set = "M10", collectorNumber = "66")
@CardRegistration(set = "POR", collectorNumber = "65")
public class PhantomWarrior extends Card {

    public PhantomWarrior() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
