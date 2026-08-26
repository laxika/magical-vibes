package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventHalfDamageToControllerAndTheirPermanentsEffect;

@CardRegistration(set = "RAV", collectorNumber = "20")
public class GhostsOfTheInnocent extends Card {

    public GhostsOfTheInnocent() {
        addEffect(EffectSlot.STATIC, new PreventHalfDamageToControllerAndTheirPermanentsEffect());
    }
}
