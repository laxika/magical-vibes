package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "129")
public class KindredBoon extends Card {

    public KindredBoon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.DIVINITY)));

        PermanentAllOfPredicate chosenCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentHasSourceChosenSubtypePredicate()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.DIVINITY, 1, chosenCreatureYouControl)),
                "{1}{W}: Put a divinity counter on target creature you control of the chosen type.",
                new PermanentPredicateTargetFilter(
                        chosenCreatureYouControl,
                        "Target must be a creature you control of the chosen type")));
    }
}
