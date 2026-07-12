package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "8ED", collectorNumber = "211")
public class Pyrotechnics extends Card {

    public Pyrotechnics() {
        // Pyrotechnics deals 4 damage divided as you choose among any number of targets.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(4));
    }
}
