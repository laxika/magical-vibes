package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MH1", collectorNumber = "109")
public class SmitingHelix extends Card {

    public SmitingHelix() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        addSpellEffect(new GainLifeEffect(3));
        addCastingOption(new FlashbackCast("{R}{W}"));
    }
}
