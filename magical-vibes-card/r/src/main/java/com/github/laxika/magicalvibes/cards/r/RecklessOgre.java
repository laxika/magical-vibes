package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "98")
public class RecklessOgre extends Card {

    public RecklessOgre() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                BoostEffect.self(3, 0));
    }
}
