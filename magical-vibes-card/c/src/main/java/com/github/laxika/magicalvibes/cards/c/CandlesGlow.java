package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

@CardRegistration(set = "CHK", collectorNumber = "5")
public class CandlesGlow extends Card {

    public CandlesGlow() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTargetAndGainLife(3));
        addEffect(EffectSlot.STATIC, new SpliceEffect(CardSubtype.ARCANE, "{1}{W}"));
    }
}
