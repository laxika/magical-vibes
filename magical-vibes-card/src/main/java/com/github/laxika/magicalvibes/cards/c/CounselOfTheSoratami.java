package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "76")
public class CounselOfTheSoratami extends Card {

    public CounselOfTheSoratami() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
