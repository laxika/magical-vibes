package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;

@CardRegistration(set = "RNA", collectorNumber = "99")
@CardRegistration(set = "OTP", collectorNumber = "23")
@CardRegistration(set = "TDC", collectorNumber = "212")
public class Electrodominance extends Card {

    public Electrodominance() {
        addSpellEffect(DealDamageEffect.toAnyTarget(new XValue()));
        addSpellEffect(new MayCastAnySpellFromHandWithoutPayingManaCostEffect(new XValue()));
    }
}
