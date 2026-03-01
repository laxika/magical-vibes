package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureXEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "131")
public class UntamedMight extends Card {

    public UntamedMight() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureXEffect(1, 1));
    }
}
