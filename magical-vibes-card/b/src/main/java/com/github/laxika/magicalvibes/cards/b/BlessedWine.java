package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "5ED", collectorNumber = "11")
@CardRegistration(set = "ICE", collectorNumber = "7")
public class BlessedWine extends Card {

    public BlessedWine() {
        // "You gain 1 life."
        addSpellEffect(new GainLifeEffect(1));
        // "Draw a card at the beginning of the next turn's upkeep."
        addSpellEffect(new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
