package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "BRO", collectorNumber = "160")
public class HeavyweightDemolisher extends Card {

    public HeavyweightDemolisher() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{3}", new TapPermanentsEffect(Subjects.source())));

        addUnearth("{6}{R}{R}");
    }
}
