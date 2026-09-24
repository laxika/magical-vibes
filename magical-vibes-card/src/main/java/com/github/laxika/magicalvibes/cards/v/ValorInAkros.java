package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "ORI", collectorNumber = "39")
@CardRegistration(set = "A25", collectorNumber = "38")
@CardRegistration(set = "2XM", collectorNumber = "37")
public class ValorInAkros extends Card {

    public ValorInAkros() {
        // Whenever a creature you control enters, creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
