package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EachCreatureDealsPowerDamageToItselfEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "124")
public class TheAkroanWar extends Card {

    public TheAkroanWar() {
        addTrigger(Triggers.chapter(1),
                new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS));
        setSagaChapterTargetFilter(1,
                Set.of(TargetFilters.creature()));

        addTrigger(Triggers.chapter(2), GoadCreaturesUntilNextTurnEffect.mustAttackOnly(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        addTrigger(Triggers.chapter(3), new EachCreatureDealsPowerDamageToItselfEffect(
                new PermanentIsTappedPredicate()));
    }
}
