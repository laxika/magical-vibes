package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "138")
public class SagittarsVolley extends Card {

    private static final PermanentPredicate FLYING_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasKeywordPredicate(Keyword.FLYING)
    ));

    private static final PermanentPredicate OPPONENT_FLYING_CREATURE = new PermanentAllOfPredicate(List.of(
            FLYING_CREATURE,
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
    ));

    public SagittarsVolley() {
        target(new PermanentPredicateTargetFilter(
                FLYING_CREATURE,
                "Target must be a creature with flying"
        ))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new DealDamageToEachMatchingPermanentEffect(
                        1, OPPONENT_FLYING_CREATURE, EachPermanentScope.ALL_PLAYERS));
    }
}
