package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "20")
public class LukeCagePowerMan extends Card {

    public LukeCagePowerMan() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                SequenceEffect.of(
                        BoostEffect.self(2, 0),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source())));
    }
}
