package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "81")
public class DizzyingGaze extends Card {

    public DizzyingGaze() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        ));

        PermanentAllOfPredicate flyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new EnchantedCreatureDealsDamageToTargetCreatureEffect(1, flyingCreature)),
                "{R}: Enchanted creature deals 1 damage to target creature with flying.",
                new PermanentPredicateTargetFilter(flyingCreature, "Target must be a creature with flying")
        ));
    }
}
