package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "29")
public class MendingHands extends Card {

    public MendingHands() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTarget(4));
    }
}
