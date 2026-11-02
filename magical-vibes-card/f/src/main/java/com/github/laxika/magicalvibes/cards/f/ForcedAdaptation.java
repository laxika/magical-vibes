package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "120")
@CardRegistration(set = "RVR", collectorNumber = "140")
public class ForcedAdaptation extends Card {

    public ForcedAdaptation() {
        target(TargetFilters.creature())
                // At the beginning of your upkeep, put a +1/+1 counter on enchanted creature.
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
