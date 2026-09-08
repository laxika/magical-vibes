package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "PLS", collectorNumber = "81")
public class GaeasMight extends Card {

    public GaeasMight() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new BasicLandTypesAmongControlledLands(), new BasicLandTypesAmongControlledLands()));
    }
}
