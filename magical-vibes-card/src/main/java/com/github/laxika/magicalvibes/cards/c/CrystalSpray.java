package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "INV", collectorNumber = "50")
public class CrystalSpray extends Card {

    public CrystalSpray() {
        addEffect(EffectSlot.SPELL, new ChangeColorTextEffect(true, true, true, true));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
