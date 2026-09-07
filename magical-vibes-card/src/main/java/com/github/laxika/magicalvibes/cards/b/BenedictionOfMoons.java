package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "3")
public class BenedictionOfMoons extends Card {

    public BenedictionOfMoons() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_DEATH, new HauntEffect());
        addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, new GainLifeEffect(2));
    }
}
