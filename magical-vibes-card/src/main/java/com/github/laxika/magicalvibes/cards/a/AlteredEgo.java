package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "INR", collectorNumber = "228")
public class AlteredEgo extends Card {

    public AlteredEgo() {
        // This spell can't be countered.
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        // You may have this creature enter as a copy of any creature on the battlefield,
        // except it enters with X additional +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", new XValue()));
    }
}
