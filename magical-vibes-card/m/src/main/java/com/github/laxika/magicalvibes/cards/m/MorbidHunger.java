package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ODY", collectorNumber = "150")
public class MorbidHunger extends Card {

    public MorbidHunger() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        addSpellEffect(new GainLifeEffect(3));
        addCastingOption(new FlashbackCast("{7}{B}{B}"));
    }
}
