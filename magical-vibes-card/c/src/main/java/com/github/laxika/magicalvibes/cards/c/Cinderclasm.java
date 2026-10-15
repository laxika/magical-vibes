package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "ZNR", collectorNumber = "136")
public class Cinderclasm extends Card {

    public Cinderclasm() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{R}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                new MassDamageEffect(1),
                new MassDamageEffect(2)));
    }
}
