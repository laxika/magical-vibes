package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "3")
public class ArashinForemost extends Card {

    public ArashinForemost() {
        PermanentPredicate anotherWarriorYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(anotherWarriorYouControl,
                "Target must be another Warrior creature you control"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET,
                                anotherWarriorYouControl))
                .addEffect(EffectSlot.ON_ATTACK,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET,
                                anotherWarriorYouControl));
    }
}
