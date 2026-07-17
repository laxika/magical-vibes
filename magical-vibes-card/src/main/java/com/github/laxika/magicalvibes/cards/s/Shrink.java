package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "5ED", collectorNumber = "326")
public class Shrink extends Card {

    public Shrink() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-5, 0));
    }
}
