package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "TDM", collectorNumber = "128")
public class TwinBolt extends Card {

    public TwinBolt() {
        target(1, 2).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(2));
    }
}
