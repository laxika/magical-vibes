package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "121")
public class TheFirstTyrannicWar extends Card {

    public TheFirstTyrannicWar() {
        addTrigger(Triggers.chapter(1),
                new PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffect(
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
        addTrigger(Triggers.chapter(2), new DoubleCountersOnTargetPermanentEffect());
        setSagaChapterTargetFilter(2, Set.of(TargetFilters.creatureYouControl()));
        setSagaChapterTargetRequired(2);
        addTrigger(Triggers.chapter(3), new DoubleCountersOnTargetPermanentEffect());
        setSagaChapterTargetFilter(3, Set.of(TargetFilters.creatureYouControl()));
        setSagaChapterTargetRequired(3);
    }
}
