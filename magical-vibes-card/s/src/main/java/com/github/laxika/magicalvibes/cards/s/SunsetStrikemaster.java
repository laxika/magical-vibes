package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "126")
public class SunsetStrikemaster extends Card {

    public SunsetStrikemaster() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        PermanentAllOfPredicate flyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(6, flyingCreature)),
                "{2}{R}, {T}, Sacrifice Sunset Strikemaster: It deals 6 damage to target creature with flying.",
                new PermanentPredicateTargetFilter(flyingCreature, "Target must be a creature with flying")
        ));
    }
}
