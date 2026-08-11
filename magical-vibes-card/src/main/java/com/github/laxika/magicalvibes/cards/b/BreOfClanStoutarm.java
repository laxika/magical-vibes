package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastOrPutIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "207")
public class BreOfClanStoutarm extends Card {

    public BreOfClanStoutarm() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.LIFELINK), GrantScope.TARGET)),
                "{1}{W}, {T}: Another target creature you control gains flying and lifelink until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature you control"
                )
        ));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(), new ExileTopUntilNonlandMayCastOrPutIntoHandEffect()));
    }
}
