package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "113")
public class FavorOfTheWoods extends Card {

    public FavorOfTheWoods() {
        // Enchant creature. Whenever enchanted creature blocks, you (the aura's controller) gain 3 life.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_BLOCK, new GainLifeEffect(3));
    }
}
