package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect;

@CardRegistration(set = "ODY", collectorNumber = "9")
public class AvenShrine extends Card {

    public AvenShrine() {
        // Whenever a player casts a spell, that player gains life equal to the number of cards in
        // all graveyards with the same name as that spell.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new GainLifeForSameNameCardsInGraveyardsOnSpellCastEffect());
    }
}
