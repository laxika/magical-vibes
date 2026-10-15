package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKC", collectorNumber = "209")
@CardRegistration(set = "VOC", collectorNumber = "29")
@CardRegistration(set = "VOC", collectorNumber = "67")
public class DisorderInTheCourt extends Card {

    public DisorderInTheCourt() {
        targetExactlyX(TargetFilters.creature(), 100)
                .addEffect(EffectSlot.SPELL, FlickerEffect.exileTargetReturnAtEndStep(true))
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(new XValue()));
    }
}
