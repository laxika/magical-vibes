package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "19")
@CardRegistration(set = "M19", collectorNumber = "15")
@CardRegistration(set = "M20", collectorNumber = "24")
@CardRegistration(set = "KLD", collectorNumber = "20")
@CardRegistration(set = "MOM", collectorNumber = "19")
public class InspiredCharge extends Card {

    public InspiredCharge() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 1));
    }
}
