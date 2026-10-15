package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageByCreaturesExceptEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "284")
@CardRegistration(set = "MB2", collectorNumber = "520")
public class NewMasterOfArms extends Card {

    public NewMasterOfArms() {
        // Prevent all combat damage that would be dealt by blocking creatures that are tapped.
        addEffect(EffectSlot.STATIC, new PreventAllCombatDamageByCreaturesExceptEffect(
                new PermanentNotPredicate(new PermanentAllOfPredicate(List.of(
                        new PermanentIsBlockingPredicate(),
                        new PermanentIsTappedPredicate()
                )))
        ));

        // {1}{W}: Tap target creature blocking New Master of Arms.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}{W}: Tap target creature blocking this creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentBlockingSourcePredicate()
                        )),
                        "Target must be a creature blocking this creature"
                )
        ));
    }
}
