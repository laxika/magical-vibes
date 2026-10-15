package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WildEndeavorEffect;

@CardRegistration(set = "AFC", collectorNumber = "43")
public class WildEndeavor extends Card {

    public WildEndeavor() {
        addEffect(EffectSlot.SPELL, new WildEndeavorEffect());
    }
}
