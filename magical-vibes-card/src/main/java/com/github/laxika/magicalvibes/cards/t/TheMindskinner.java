package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToOpponentsAndMillEffect;

@CardRegistration(set = "DSK", collectorNumber = "66")
public class TheMindskinner extends Card {

    public TheMindskinner() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addEffect(EffectSlot.STATIC, new PreventDamageToOpponentsAndMillEffect());
    }
}
