package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "65")
public class ChorusOfWoe extends Card {

    public ChorusOfWoe() {
        // Creatures you control get +1/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 0));
    }
}
