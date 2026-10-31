package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EXO", collectorNumber = "102")
public class ShatteringPulse extends Card {

    public ShatteringPulse() {
        addStaticEffect(new BuybackEffect("{3}"));
        target(TargetFilters.artifact()).addSpellEffect(new DestroyTargetPermanentEffect());
        addSpellEffect(new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
