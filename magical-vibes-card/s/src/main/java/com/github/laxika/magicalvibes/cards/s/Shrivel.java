package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "M14", collectorNumber = "116")
@CardRegistration(set = "ROE", collectorNumber = "126")
@CardRegistration(set = "MM2", collectorNumber = "95")
public class Shrivel extends Card {

    public Shrivel() {
        // All creatures get -1/-1 until end of turn.
        addSpellEffect(BoostEffect.allCreatures(-1, -1));
    }
}
