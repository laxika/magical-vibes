package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;

@CardRegistration(set = "SOM", collectorNumber = "32")
public class DissipationField extends Card {

    public DissipationField() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, new ReturnDamageSourcePermanentToHandEffect());
    }
}
