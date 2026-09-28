package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "MH2", collectorNumber = "210")
public class RakdosHeadliner extends Card {

    public RakdosHeadliner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RegisterEchoAtNextUpkeepEffect(new DiscardCardTypeCost(null, null)));
    }
}
