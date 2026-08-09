package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "48")
public class Regress extends Card {

    public Regress() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
