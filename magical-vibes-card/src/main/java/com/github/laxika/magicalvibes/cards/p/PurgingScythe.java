package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToCreatureWithLeastToughnessEffect;

@CardRegistration(set = "USG", collectorNumber = "308")
public class PurgingScythe extends Card {

    public PurgingScythe() {
        // At the beginning of your upkeep, this artifact deals 2 damage to the creature with the least toughness.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DealDamageToCreatureWithLeastToughnessEffect(2));
    }
}
