package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "153")
public class OtepecHuntmaster extends Card {

    public OtepecHuntmaster() {
        // Dinosaur spells you cast cost {1} less to cast
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostForSubtypeEffect(
                Set.of(CardSubtype.DINOSAUR), 1));

        // {T}: Target Dinosaur gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{T}: Target Dinosaur gains haste until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.DINOSAUR)),
                        "Target must be a Dinosaur"
                )
        ));
    }
}
