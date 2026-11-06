package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "117")
public class ManaSkimmer extends Card {

    public ManaSkimmer() {
        addTrigger(Triggers.dealsDamageToPlayer(),
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET, new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentControlledByTriggeringPlayerPredicate()))),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
