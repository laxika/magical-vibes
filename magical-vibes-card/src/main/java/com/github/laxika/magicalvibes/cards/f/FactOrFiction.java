package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "INV", collectorNumber = "57")
public class FactOrFiction extends Card {

    public FactOrFiction() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsAndSeparateEffect(5));
    }
}
