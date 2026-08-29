package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "RAV", collectorNumber = "7")
public class ChantOfVituGhazi extends Card {

    public ChantOfVituGhazi() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allByCreaturesAndGainLife());
    }
}
