package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentsThenEachPlayerRevealsUntilPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DSC", collectorNumber = "329")
public class ChaosIsMyPlaything extends Card {

    public ChaosIsMyPlaything() {
        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_OPPONENT);
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                "Target must be a permanent an opponent controls"), 1, Integer.MAX_VALUE)
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetPermanentsThenEachPlayerRevealsUntilPermanentEffect());
    }
}
