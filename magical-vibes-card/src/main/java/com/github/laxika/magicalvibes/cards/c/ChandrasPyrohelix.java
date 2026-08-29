package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "KLD", collectorNumber = "111")
@CardRegistration(set = "WAR", collectorNumber = "120")
public class ChandrasPyrohelix extends Card {

    public ChandrasPyrohelix() {
        // Chandra's Pyrohelix deals 2 damage divided as you choose among one or two targets.
        target(1, 2).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(2));
    }
}
