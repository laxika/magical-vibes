package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

@CardRegistration(set = "M19", collectorNumber = "166")
public class ViashinoPyromancer extends Card {

    public ViashinoPyromancer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetPlayerOrPlaneswalkerEffect(2));
    }
}
