package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "82")
public class EssenceFracture extends Card {

    public EssenceFracture() {
        target(TargetFilters.creature(), 2, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addCycling("{2}{U}");
    }
}
