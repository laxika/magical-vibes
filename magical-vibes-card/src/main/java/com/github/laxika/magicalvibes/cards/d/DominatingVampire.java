package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "VOW", collectorNumber = "154")
public class DominatingVampire extends Card {

    public DominatingVampire() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                GainControlOfTargetEffect.withTargetPredicate(
                        ControlDuration.END_OF_TURN,
                        new PermanentManaValueAtMostControlledCountPredicate(
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE))))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
