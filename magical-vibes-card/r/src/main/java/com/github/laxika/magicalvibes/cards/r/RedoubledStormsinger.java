package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachTokenEnteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "251")
@CardRegistration(set = "TDC", collectorNumber = "37")
@CardRegistration(set = "TDC", collectorNumber = "77")
public class RedoubledStormsinger extends Card {

    public RedoubledStormsinger() {
        addTrigger(Triggers.attacks(), new CreateTokenCopyOfEachTokenEnteredThisTurnEffect());
    }
}
