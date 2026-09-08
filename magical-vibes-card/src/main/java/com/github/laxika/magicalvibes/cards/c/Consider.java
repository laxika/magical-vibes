package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "MID", collectorNumber = "44")
public class Consider extends Card {

    public Consider() {
        addEffect(EffectSlot.SPELL, new SurveilEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
