package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "76")
public class ZephyrSpirit extends Card {

    public ZephyrSpirit() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, ReturnToHandEffect.self());
    }
}
