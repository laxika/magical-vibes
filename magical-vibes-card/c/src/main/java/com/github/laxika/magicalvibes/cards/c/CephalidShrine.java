package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect;

@CardRegistration(set = "ODY", collectorNumber = "75")
public class CephalidShrine extends Card {

    public CephalidShrine() {
        // Whenever a player casts a spell, counter that spell unless that player pays X, where X
        // is the number of cards in all graveyards with the same name as the spell.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CounterUnlessPaysForSameNameCardsInGraveyardsOnSpellCastEffect());
    }
}
