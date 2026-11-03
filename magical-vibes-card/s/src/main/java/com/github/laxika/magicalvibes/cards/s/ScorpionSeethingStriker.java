package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "64")
@CardRegistration(set = "OM1", collectorNumber = "68")
public class ScorpionSeethingStriker extends Card {

    public ScorpionSeethingStriker() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Morbid()),
                new DrawDiscardAndConniveEffect(true));
    }
}
