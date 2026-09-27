package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "303")
public class SidarKondoOfJamuraa extends Card {

    public SidarKondoOfJamuraa() {
        // Creatures your opponents control without flying or reach can't block creatures with power 2 or less.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.REACH)))),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtMostPredicate(2))),
                "Creatures your opponents control without flying or reach can't block creatures with power 2 or less"));
    }
}
