package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "170")
public class TakeDown extends Card {

    public TakeDown() {
        PermanentPredicateTargetFilter flyingCreatureFilter = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING)
                )),
                "Target must be a creature with flying."
        );
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Take Down deals 4 damage to target creature with flying",
                        DealDamageEffect.toTargetCreature(4),
                        flyingCreatureFilter
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Take Down deals 1 damage to each creature with flying",
                        DealDamageEffect.toEachCreature(1, new PermanentHasKeywordPredicate(Keyword.FLYING))
                )
        )));
    }
}
