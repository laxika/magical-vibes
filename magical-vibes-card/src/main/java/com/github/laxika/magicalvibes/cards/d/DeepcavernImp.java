package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "FUT", collectorNumber = "83")
public class DeepcavernImp extends Card {

    public DeepcavernImp() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RegisterEchoAtNextUpkeepEffect(new DiscardCardTypeCost(null, null)));
    }
}
