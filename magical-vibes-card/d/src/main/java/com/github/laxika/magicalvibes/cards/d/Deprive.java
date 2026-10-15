package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ROE", collectorNumber = "59")
public class Deprive extends Card {

    public Deprive() {
        // As an additional cost to cast this spell, return a land you control to its owner's hand.
        addEffect(EffectSlot.SPELL, new ReturnPermanentToHandCost(new PermanentIsLandPredicate()));
        // Counter target spell.
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
