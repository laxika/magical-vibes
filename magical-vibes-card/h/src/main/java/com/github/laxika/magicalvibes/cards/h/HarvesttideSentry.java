package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "186")
@CardRegistration(set = "DBL", collectorNumber = "186")
public class HarvesttideSentry extends Card {

    public HarvesttideSentry() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Coven()),
                new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentPowerAtLeastPredicate(3),
                        "creatures with power 3 or greater",
                        true));
    }
}
