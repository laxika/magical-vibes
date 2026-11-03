package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "22")
public class MouseTrapper extends Card {

    public MouseTrapper() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.becomesTargetOfYourSpellOrAbility(),
                        TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                        new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
