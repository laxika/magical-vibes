package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "TSP", collectorNumber = "154")
public class FlamecoreElemental extends Card {

    public FlamecoreElemental() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{2}{R}{R}"));
    }
}
