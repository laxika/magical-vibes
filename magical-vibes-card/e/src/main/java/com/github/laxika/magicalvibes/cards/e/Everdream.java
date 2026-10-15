package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

@CardRegistration(set = "MH1", collectorNumber = "47")
public class Everdream extends Card {

    public Everdream() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
        addEffect(EffectSlot.STATIC, SpliceEffect.ontoInstantOrSorcery("{2}{U}"));
    }
}
