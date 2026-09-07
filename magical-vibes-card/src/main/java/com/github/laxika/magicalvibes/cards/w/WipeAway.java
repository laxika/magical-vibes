package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "94")
public class WipeAway extends Card {

    public WipeAway() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
