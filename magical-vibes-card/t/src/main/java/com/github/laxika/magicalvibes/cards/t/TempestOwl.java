package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "72")
public class TempestOwl extends Card {

    public TempestOwl() {
        addStaticEffect(new KickerEffect("{4}{U}"));
        target(TargetFilters.permanent(), 0, 3)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                        new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
