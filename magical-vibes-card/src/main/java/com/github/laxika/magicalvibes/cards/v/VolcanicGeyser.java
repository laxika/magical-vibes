package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "6ED", collectorNumber = "215")
@CardRegistration(set = "MIR", collectorNumber = "202")
@CardRegistration(set = "M13", collectorNumber = "154")
public class VolcanicGeyser extends Card {

    public VolcanicGeyser() {
        // Volcanic Geyser deals X damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
    }
}
