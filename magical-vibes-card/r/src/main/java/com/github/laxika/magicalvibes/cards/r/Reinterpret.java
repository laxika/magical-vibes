package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;

@CardRegistration(set = "C21", collectorNumber = "73")
public class Reinterpret extends Card {

    public Reinterpret() {
        // The counter remembers the countered spell's mana value for the hand-spell cap that follows.
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL,
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(new TargetSpellManaValue()));
    }
}
