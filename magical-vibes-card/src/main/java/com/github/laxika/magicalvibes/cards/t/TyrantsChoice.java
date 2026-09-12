package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TyrantsChoiceEffect;

@CardRegistration(set = "VMA", collectorNumber = "143")
public class TyrantsChoice extends Card {

    public TyrantsChoice() {
        addEffect(EffectSlot.SPELL, new TyrantsChoiceEffect());
    }
}
