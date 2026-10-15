package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "121")
public class TheFirstTyrannicWar extends Card {

    public TheFirstTyrannicWar() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffect(
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DoubleCountersOnTargetPermanentEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(TargetFilters.creatureYouControl()));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new DoubleCountersOnTargetPermanentEffect());
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(TargetFilters.creatureYouControl()));
    }
}
