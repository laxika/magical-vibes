package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "FDN", collectorNumber = "8")
public class DauntlessVeteran extends Card {

    public DauntlessVeteran() {
        // Whenever this creature attacks, creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
