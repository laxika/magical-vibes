package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RunThePlay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Striding Shotcaller // Run the Play (SOC 50). */
@CardRegistration(set = "SOC", collectorNumber = "50")
@CardRegistration(set = "SOC", collectorNumber = "98")
public class StridingShotcallerRunThePlay extends Card {

    public StridingShotcallerRunThePlay() {
        setBackFaceCard(new RunThePlay());

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.ONE_OR_MORE,
                new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "RunThePlay";
    }
}
