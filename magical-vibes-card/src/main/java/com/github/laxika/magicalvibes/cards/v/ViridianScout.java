package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "100")
public class ViridianScout extends Card {

    public ViridianScout() {
        PermanentAllOfPredicate flyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(2, flyingCreature)),
                "{2}{G}, Sacrifice this creature: It deals 2 damage to target creature with flying.",
                new PermanentPredicateTargetFilter(flyingCreature, "Target must be a creature with flying")
        ));
    }
}
