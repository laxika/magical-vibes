package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "20")
@CardRegistration(set = "DDF", collectorNumber = "12")
@CardRegistration(set = "DDN", collectorNumber = "52")
@CardRegistration(set = "EMA", collectorNumber = "18")
@CardRegistration(set = "MM3", collectorNumber = "10")
public class KorHookmaster extends Card {

    public KorHookmaster() {
        // When this creature enters, tap target creature an opponent controls.
        // That creature doesn't untap during its controller's next untap step.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
