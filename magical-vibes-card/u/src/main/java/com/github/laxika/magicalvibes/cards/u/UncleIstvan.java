package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;

@CardRegistration(set = "4ED", collectorNumber = "165")
@CardRegistration(set = "DRK", collectorNumber = "54")
@CardRegistration(set = "TSB", collectorNumber = "51")
public class UncleIstvan extends Card {

    public UncleIstvan() {
        // "Prevent all damage that would be dealt to this creature by creatures."
        addEffect(EffectSlot.STATIC, new PreventDamageToSelfFromCreaturesEffect());
    }
}
