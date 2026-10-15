package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M15", collectorNumber = "164")
@CardRegistration(set = "MOM", collectorNumber = "166")
public class StokeTheFlames extends Card {

    public StokeTheFlames() {
        // Convoke is granted by the Scryfall-loaded keyword and handled by the casting service.
        // "Stoke the Flames deals 4 damage to any target."
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
    }
}
