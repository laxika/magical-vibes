package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.HullbreakerHorrorTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "68")
public class HullbreakerHorror extends Card {

    public HullbreakerHorror() {
        // Flash — keyword from Scryfall.
        // This spell can't be countered.
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        // Whenever you cast a spell, choose up to one —
        // • Return target spell you don't control to its owner's hand.
        // • Return target nonland permanent to its owner's hand.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new HullbreakerHorrorTriggerEffect())));
    }
}
