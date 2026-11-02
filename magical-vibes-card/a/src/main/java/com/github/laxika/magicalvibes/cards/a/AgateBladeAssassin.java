package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "82")
public class AgateBladeAssassin extends Card {

    public AgateBladeAssassin() {
        addTrigger(Triggers.attacks(), new LoseLifeEffect(1, LoseLifeRecipient.DEFENDING_PLAYER));
        addTrigger(Triggers.attacks(), new GainLifeEffect(1));
    }
}
