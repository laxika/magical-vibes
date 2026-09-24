package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ARB", collectorNumber = "46")
@CardRegistration(set = "PLS", collectorNumber = "128")
@CardRegistration(set = "DDK", collectorNumber = "64")
@CardRegistration(set = "MM3", collectorNumber = "194")
@CardRegistration(set = "SLD", collectorNumber = "1427")
@CardRegistration(set = "2X2", collectorNumber = "284")
@CardRegistration(set = "ACR", collectorNumber = "98")
@CardRegistration(set = "MAR", collectorNumber = "40")
@CardRegistration(set = "OMB", collectorNumber = "40")
@CardRegistration(set = "MSC", collectorNumber = "189")
@CardRegistration(set = "ECC", collectorNumber = "134")
@CardRegistration(set = "CMD", collectorNumber = "231")
public class Terminate extends Card {

    public Terminate() {
        // Destroy target creature. It can't be regenerated.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(true));
    }
}
