package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "SNC", collectorNumber = "100")
public class Antagonize extends Card {

    public Antagonize() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 3));
    }
}
