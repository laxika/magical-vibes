package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

@CardRegistration(set = "5ED", collectorNumber = "288")
@CardRegistration(set = "4ED", collectorNumber = "240")
public class DesertTwister extends Card {

    public DesertTwister() {
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
