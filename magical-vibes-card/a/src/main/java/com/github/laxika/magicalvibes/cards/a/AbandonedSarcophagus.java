package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CastSpellsWithCyclingFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCyclingCardsUnlessCycledEffect;

@CardRegistration(set = "HOU", collectorNumber = "158")
@CardRegistration(set = "AKR", collectorNumber = "268")
@CardRegistration(set = "C20", collectorNumber = "236")
public class AbandonedSarcophagus extends Card {

    public AbandonedSarcophagus() {
        // You may cast spells that have a cycling ability from your graveyard.
        addStaticEffect(new CastSpellsWithCyclingFromGraveyardEffect());
        // If a card that has a cycling ability would be put into your graveyard from anywhere
        // and it wasn't cycled, exile it instead.
        addStaticEffect(new ExileOwnCyclingCardsUnlessCycledEffect());
    }
}
