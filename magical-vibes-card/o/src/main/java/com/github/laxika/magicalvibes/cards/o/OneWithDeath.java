package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;

@CardRegistration(set = "MB1", collectorNumber = "45")
public class OneWithDeath extends Card {

    public OneWithDeath() {
        addSpellEffect(new ControllerLosesGameEffect());
    }
}
