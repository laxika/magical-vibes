package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastSpellsWithCyclingFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCyclingCardsUnlessCycledEffect;

@CardRegistration(set = "HOU", collectorNumber = "158")
public class AbandonedSarcophagus extends Card {

    public AbandonedSarcophagus() {
        // You may cast spells that have a cycling ability from your graveyard.
        addEffect(EffectSlot.STATIC, new CastSpellsWithCyclingFromGraveyardEffect());
        // If a card that has a cycling ability would be put into your graveyard from anywhere
        // and it wasn't cycled, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileOwnCyclingCardsUnlessCycledEffect());
    }
}
