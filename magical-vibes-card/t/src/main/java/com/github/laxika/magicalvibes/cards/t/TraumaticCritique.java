package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOS", collectorNumber = "239")
public class TraumaticCritique extends Card {

    public TraumaticCritique() {
        addSpellEffect(DealDamageEffect.toAnyTarget(new XValue()));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(1, Subjects.you()));
    }
}
