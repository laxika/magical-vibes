package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "74")
@CardRegistration(set = "A25", collectorNumber = "48")
@CardRegistration(set = "VMA", collectorNumber = "59")
@CardRegistration(set = "MH1", collectorNumber = "44")
public class ChokingTethers extends Card {

    public ChokingTethers() {
        target(TargetFilters.creature(), 0, 4)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET));

        addCycling("{1}{U}");
        // "When you cycle this card, you may tap target creature": the ability's own target, apart from the spell's.
        addTriggeredAbility(new TriggeredAbility(Triggers.youCycleThisCard(), new MayEffect(
                new TapPermanentsEffect(TapUntapScope.TARGET), "Tap target creature?"))
                .withTargetFilter(TargetFilters.creature()));
    }
}
