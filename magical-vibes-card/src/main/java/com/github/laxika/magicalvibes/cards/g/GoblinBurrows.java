package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "318")
public class GoblinBurrows extends Card {

    private static final PermanentAllOfPredicate GOBLIN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
    ));

    public GoblinBurrows() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new BoostTargetCreatureEffect(2, 0, GOBLIN_CREATURE)),
                "{1}{R}, {T}: Target Goblin creature gets +2/+0 until end of turn.",
                new PermanentPredicateTargetFilter(GOBLIN_CREATURE, "Target must be a Goblin creature")
        ));
    }
}
