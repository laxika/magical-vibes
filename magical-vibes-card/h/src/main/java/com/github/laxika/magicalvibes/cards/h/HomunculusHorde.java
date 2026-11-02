package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "41")
@CardRegistration(set = "FDN", collectorNumber = "308")
@CardRegistration(set = "FDN", collectorNumber = "376")
@CardRegistration(set = "FDN", collectorNumber = "454")
public class HomunculusHorde extends Card {

    public HomunculusHorde() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), new CreateTokenCopyOfSourceEffect());
    }
}
