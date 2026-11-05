package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "94")
public class LunkErrant extends Card {

    public LunkErrant() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new BoostSelfEffect(1, 1),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));
    }
}
