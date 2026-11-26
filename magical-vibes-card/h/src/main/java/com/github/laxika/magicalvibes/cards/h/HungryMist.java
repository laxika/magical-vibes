package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "5ED", collectorNumber = "302")
@CardRegistration(set = "HML", collectorNumber = "88a")
@CardRegistration(set = "HML", collectorNumber = "88b")
@CardRegistration(set = "ME1", collectorNumber = "121")
public class HungryMist extends Card {

    public HungryMist() {
        // At the beginning of your upkeep, sacrifice this creature unless you pay {G}{G}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{G}{G}", new SacrificeSelfEffect()));
    }
}
