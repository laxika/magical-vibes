package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;

@CardRegistration(set = "ONS", collectorNumber = "53")
public class SharedTriumph extends Card {

    public SharedTriumph() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(new BoostCreaturesOfChosenSubtypeEffect(1, 1, null, true));
    }
}
