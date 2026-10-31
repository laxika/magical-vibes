package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "EXO", collectorNumber = "35")
@CardRegistration(set = "TPR", collectorNumber = "50")
@CardRegistration(set = "MP2", collectorNumber = "33")
public class Forbid extends Card {

    public Forbid() {
        addStaticEffect(new BuybackEffect(2));
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
