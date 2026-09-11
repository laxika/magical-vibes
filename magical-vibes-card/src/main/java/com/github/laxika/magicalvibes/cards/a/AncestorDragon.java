package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "FDN", collectorNumber = "489")
public class AncestorDragon extends Card {

    public AncestorDragon() {
        // Whenever one or more creatures you control attack, you gain 1 life for each attacking creature.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new GainLifeEffect(new XValue()));
    }
}
