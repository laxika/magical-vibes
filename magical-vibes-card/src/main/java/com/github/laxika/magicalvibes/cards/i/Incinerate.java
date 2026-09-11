package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "213")
@CardRegistration(set = "5ED", collectorNumber = "242")
@CardRegistration(set = "ICE", collectorNumber = "194")
@CardRegistration(set = "M12", collectorNumber = "146")
@CardRegistration(set = "MIR", collectorNumber = "184")
@CardRegistration(set = "DKM", collectorNumber = "14")
@CardRegistration(set = "DD2", collectorNumber = "51")
@CardRegistration(set = "CST", collectorNumber = "51")
@CardRegistration(set = "DPA", collectorNumber = "48")
@CardRegistration(set = "ME2", collectorNumber = "131")
public class Incinerate extends Card {

    public Incinerate() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3, true));
    }
}
