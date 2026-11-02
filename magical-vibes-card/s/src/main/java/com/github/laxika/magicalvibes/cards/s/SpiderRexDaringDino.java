package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "116")
@CardRegistration(set = "OM1", collectorNumber = "100")
@CardRegistration(set = "MSC", collectorNumber = "818")
public class SpiderRexDaringDino extends Card {

    public SpiderRexDaringDino() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(2));
    }
}
