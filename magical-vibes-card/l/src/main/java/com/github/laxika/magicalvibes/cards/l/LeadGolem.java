package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "296")
@CardRegistration(set = "MIR", collectorNumber = "306")
public class LeadGolem extends Card {

    public LeadGolem() {
        // Whenever Lead Golem attacks, it doesn't untap during its controller's next untap step.
        addTrigger(Triggers.attacks(), new SkipNextUntapEffect(TapUntapScope.SELF));
    }
}
