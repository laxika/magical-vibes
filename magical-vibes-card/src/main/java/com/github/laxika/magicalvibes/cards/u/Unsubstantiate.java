package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrCreatureToHandEffect;

@CardRegistration(set = "EMN", collectorNumber = "79")
public class Unsubstantiate extends Card {

    public Unsubstantiate() {
        addEffect(EffectSlot.SPELL, new ReturnTargetSpellOrCreatureToHandEffect());
    }
}
