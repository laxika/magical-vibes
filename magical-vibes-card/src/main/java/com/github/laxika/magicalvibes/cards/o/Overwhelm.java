package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "M15", collectorNumber = "189")
@CardRegistration(set = "RAV", collectorNumber = "175")
public class Overwhelm extends Card {

    public Overwhelm() {
        // Convoke is granted by the Scryfall-loaded keyword and handled by the casting service.
        // "Creatures you control get +3/+3 until end of turn."
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(3, 3));
    }
}
