package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "GPT", collectorNumber = "21")
public class Withstand extends Card {

    public Withstand() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTarget(3));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
