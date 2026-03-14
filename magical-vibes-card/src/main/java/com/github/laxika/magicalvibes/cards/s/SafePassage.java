package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndCreaturesEffect;

@CardRegistration(set = "M10", collectorNumber = "28")
@CardRegistration(set = "M11", collectorNumber = "26")
public class SafePassage extends Card {

    public SafePassage() {
        addEffect(EffectSlot.SPELL, new PreventAllDamageToControllerAndCreaturesEffect());
    }
}
