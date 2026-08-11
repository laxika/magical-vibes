package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "THS", collectorNumber = "115")
public class Boulderfall extends Card {

    public Boulderfall() {
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(5));
    }
}
