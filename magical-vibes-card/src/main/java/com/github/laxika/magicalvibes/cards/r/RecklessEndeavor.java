package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RecklessEndeavorEffect;

@CardRegistration(set = "SLD", collectorNumber = "2177")
public class RecklessEndeavor extends Card {

    public RecklessEndeavor() {
        addEffect(EffectSlot.SPELL, new RecklessEndeavorEffect());
    }
}
