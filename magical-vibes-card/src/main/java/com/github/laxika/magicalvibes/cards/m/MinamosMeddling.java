package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndDiscardSplicedNamesEffect;

@CardRegistration(set = "BOK", collectorNumber = "42")
public class MinamosMeddling extends Card {

    public MinamosMeddling() {
        // Counter target spell. That spell's controller reveals their hand, then discards each card
        // with the same name as a card spliced onto that spell.
        addEffect(EffectSlot.SPELL, new CounterSpellAndDiscardSplicedNamesEffect());
    }
}
