package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ValiantEndeavorEffect;

@CardRegistration(set = "AFC", collectorNumber = "13")
public class ValiantEndeavor extends Card {

    public ValiantEndeavor() {
        addEffect(EffectSlot.SPELL, new ValiantEndeavorEffect());
    }
}
