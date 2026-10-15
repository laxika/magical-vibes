package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ULG", collectorNumber = "109")
public class MultanisPresence extends Card {

    public MultanisPresence() {
        addEffect(EffectSlot.ON_CONTROLLER_SPELL_COUNTERED, new DrawCardEffect());
    }
}
