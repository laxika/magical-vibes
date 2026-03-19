package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DOM", collectorNumber = "45")
public class Befuddle extends Card {

    public Befuddle() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, 0));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
