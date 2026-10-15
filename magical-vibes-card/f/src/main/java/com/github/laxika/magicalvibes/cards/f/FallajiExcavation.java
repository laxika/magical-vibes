package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "BRO", collectorNumber = "178")
public class FallajiExcavation extends Card {

    public FallajiExcavation() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofPowerstoneToken(new Fixed(3)));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
