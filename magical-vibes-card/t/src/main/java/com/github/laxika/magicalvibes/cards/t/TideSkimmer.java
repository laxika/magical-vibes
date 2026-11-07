package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "79")
@CardRegistration(set = "SCD", collectorNumber = "62")
public class TideSkimmer extends Card {

    public TideSkimmer() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(
                        new MinimumMatchingAttackers(2, new PermanentHasKeywordPredicate(Keyword.FLYING))),
                new DrawCardEffect());
    }
}
