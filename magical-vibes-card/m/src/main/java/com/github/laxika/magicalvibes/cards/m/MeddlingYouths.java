package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "219")
@CardRegistration(set = "MKM", collectorNumber = "367")
public class MeddlingYouths extends Card {

    public MeddlingYouths() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withInterveningIf(new MinimumAttackers(3)),
                CreateTokenEffect.ofClueToken(1));
    }
}
