package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetSpellManaValueAtMostGreatestControlledPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "SCG", collectorNumber = "33")
public class DispersalShield extends Card {

    public DispersalShield() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetSpellManaValueAtMostGreatestControlledPermanentManaValue(),
                new CounterSpellEffect()));
    }
}
