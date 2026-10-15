package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "DMU", collectorNumber = "116")
public class BalduvianBerserker extends Card {

    public BalduvianBerserker() {
        // Enlist is loaded from Scryfall and handled during attacker declaration.
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
