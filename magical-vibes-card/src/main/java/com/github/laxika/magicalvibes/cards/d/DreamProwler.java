package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "74")
@CardRegistration(set = "STH", collectorNumber = "29")
public class DreamProwler extends Card {

    public DreamProwler() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfAttackingAloneEffect());
    }
}
