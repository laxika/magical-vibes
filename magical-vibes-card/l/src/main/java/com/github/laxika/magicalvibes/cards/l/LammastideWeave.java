package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NameCardMillTargetGainLifeEffect;

@CardRegistration(set = "LRW", collectorNumber = "226")
public class LammastideWeave extends Card {

    public LammastideWeave() {
        addEffect(EffectSlot.SPELL, new NameCardMillTargetGainLifeEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
