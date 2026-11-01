package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

@CardRegistration(set = "INV", collectorNumber = "297")
public class AlloyGolem extends Card {

    public AlloyGolem() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addStaticEffect(new BecomeChosenColorEffect());
    }
}
