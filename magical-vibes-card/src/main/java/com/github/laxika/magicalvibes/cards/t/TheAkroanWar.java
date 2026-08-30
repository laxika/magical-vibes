package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EachCreatureDealsPowerDamageToItselfEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "124")
public class TheAkroanWar extends Card {

    public TheAkroanWar() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I,
                Set.of(TargetFilters.creatureAnOpponentControls()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new GoadCreaturesUntilNextTurnEffect(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new EachCreatureDealsPowerDamageToItselfEffect(
                new PermanentIsTappedPredicate()));
    }
}
