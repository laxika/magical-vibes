package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawOnCreatureSpellCastThisTurnEffect;

@CardRegistration(set = "CHK", collectorNumber = "210")
public class GlimpseOfNature extends Card {

    public GlimpseOfNature() {
        // Whenever you cast a creature spell this turn, draw a card.
        addEffect(EffectSlot.SPELL, new DrawOnCreatureSpellCastThisTurnEffect());
    }
}
