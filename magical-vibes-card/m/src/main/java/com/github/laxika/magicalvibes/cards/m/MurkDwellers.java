package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "180")
@CardRegistration(set = "4ED", collectorNumber = "148")
@CardRegistration(set = "ITP", collectorNumber = "21")
@CardRegistration(set = "RQS", collectorNumber = "20")
@CardRegistration(set = "DRK", collectorNumber = "49")
public class MurkDwellers extends Card {

    public MurkDwellers() {
        addTrigger(Triggers.attacksUnblocked(),
                new BoostSelfEffect(2, 0, EffectDuration.UNTIL_END_OF_COMBAT));
    }
}
