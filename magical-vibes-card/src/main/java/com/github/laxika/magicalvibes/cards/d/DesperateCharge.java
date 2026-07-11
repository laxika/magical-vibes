package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "74")
public class DesperateCharge extends Card {

    public DesperateCharge() {
        // Creatures you control get +2/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 0));
    }
}
