package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "122")
public class Unsummon extends Card {

    public Unsummon() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ReturnTargetCreatureToHandEffect());
    }
}
