package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "VIS", collectorNumber = "107")
public class FeralInstinct extends Card {

    public FeralInstinct() {
        // "Target creature gets +1/+1 until end of turn."
        addSpellEffect(BoostEffect.target(1, 1));
        // "Draw a card at the beginning of the next turn's upkeep."
        addSpellEffect(new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
