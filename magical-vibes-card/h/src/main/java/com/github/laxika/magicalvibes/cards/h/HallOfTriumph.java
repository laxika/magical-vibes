package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

@CardRegistration(set = "JOU", collectorNumber = "162")
public class HallOfTriumph extends Card {

    public HallOfTriumph() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addStaticEffect(new BoostCreaturesOfChosenColorEffect(1, 1));
    }
}
