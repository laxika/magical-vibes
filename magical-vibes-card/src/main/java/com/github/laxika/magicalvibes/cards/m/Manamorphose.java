package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SHM", collectorNumber = "211")
public class Manamorphose extends Card {

    public Manamorphose() {
        // Add two mana in any combination of colors — each mana's color is chosen independently,
        // so two separate one-mana any-color choices reproduce the "any combination" wording.
        addEffect(EffectSlot.SPELL, new AwardAnyColorManaEffect(1));
        addEffect(EffectSlot.SPELL, new AwardAnyColorManaEffect(1));
        // Draw a card.
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
