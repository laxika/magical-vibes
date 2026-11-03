package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "74")
public class WavecrashTriton extends Card {

    public WavecrashTriton() {
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
