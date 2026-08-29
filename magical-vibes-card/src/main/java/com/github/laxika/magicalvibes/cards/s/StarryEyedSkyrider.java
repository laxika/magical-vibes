package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "25")
public class StarryEyedSkyrider extends Card {

    public StarryEyedSkyrider() {
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new ControlledPermanentPredicateTargetFilter(
                targetFilter,
                "Target must be another creature you control"
        )).addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(),
                        new PermanentIsAttackingPredicate()
                ))));
    }
}
