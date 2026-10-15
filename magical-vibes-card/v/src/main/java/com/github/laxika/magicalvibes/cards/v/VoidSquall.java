package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "83")
public class VoidSquall extends Card {

    public VoidSquall() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
