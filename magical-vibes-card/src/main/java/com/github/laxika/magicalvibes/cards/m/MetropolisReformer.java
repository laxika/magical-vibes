package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;

@CardRegistration(set = "MAT", collectorNumber = "4")
@CardRegistration(set = "FDC", collectorNumber = "40")
@CardRegistration(set = "MAT", collectorNumber = "54")
@CardRegistration(set = "MAT", collectorNumber = "104")
@CardRegistration(set = "MAT", collectorNumber = "152")
@CardRegistration(set = "MAT", collectorNumber = "189")
public class MetropolisReformer extends Card {

    public MetropolisReformer() {
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
