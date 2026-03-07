package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RequirePhyrexianPaymentToAttackEffect;

@CardRegistration(set = "NPH", collectorNumber = "17")
public class NornsAnnex extends Card {

    public NornsAnnex() {
        addEffect(EffectSlot.STATIC, new RequirePhyrexianPaymentToAttackEffect(ManaColor.WHITE));
    }
}
