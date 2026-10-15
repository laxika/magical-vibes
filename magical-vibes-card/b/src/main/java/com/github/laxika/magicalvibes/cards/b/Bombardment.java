package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BombardmentEffect;

@CardRegistration(set = "MB1", collectorNumber = "53")
public class Bombardment extends Card {

    public Bombardment() {
        addEffect(EffectSlot.SPELL, new BombardmentEffect());
    }
}
