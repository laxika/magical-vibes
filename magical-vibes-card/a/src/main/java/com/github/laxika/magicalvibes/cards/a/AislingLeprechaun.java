package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "173")
public class AislingLeprechaun extends Card {

    public AislingLeprechaun() {
        addTrigger(Triggers.blocks(), new SetTargetColorEffect(CardColor.GREEN));
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new SetTargetColorEffect(CardColor.GREEN));
    }
}
