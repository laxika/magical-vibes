package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "EMN", collectorNumber = "143")
public class SpreadingFlames extends Card {

    public SpreadingFlames() {
        // Spreading Flames deals 6 damage divided as you choose among any number of target creatures.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(6));
    }
}
