package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessCountAlsoDoesEffect;

@CardRegistration(set = "5ED", collectorNumber = "255")
public class OrcishConscripts extends Card {

    public OrcishConscripts() {
        // Can't attack unless at least two other creatures attack;
        // can't block unless at least two other creatures block.
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessCountAlsoDoesEffect(2));
    }
}
