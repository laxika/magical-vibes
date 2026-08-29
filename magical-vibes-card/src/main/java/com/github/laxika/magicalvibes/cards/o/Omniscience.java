package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;

@CardRegistration(set = "M13", collectorNumber = "63")
@CardRegistration(set = "M19", collectorNumber = "65")
@CardRegistration(set = "FDN", collectorNumber = "161")
public class Omniscience extends Card {

    public Omniscience() {
        // "You may cast spells from your hand without paying their mana costs." — an alternative cost
        // of {0} for any spell, restricted to casts from the controller's hand.
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect("{0}", null, null, false, true));
    }
}
