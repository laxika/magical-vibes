package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "M15", collectorNumber = "175")
@CardRegistration(set = "RAV", collectorNumber = "165")
public class GatherCourage extends Card {

    public GatherCourage() {
        // Convoke is granted by the Scryfall-loaded keyword and handled by the casting service.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
    }
}
