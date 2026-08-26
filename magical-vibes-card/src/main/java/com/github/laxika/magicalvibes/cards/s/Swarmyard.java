package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "278")
public class Swarmyard extends Card {

    public Swarmyard() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        var protectedTypes = new PermanentHasAnySubtypePredicate(Set.of(
                CardSubtype.INSECT,
                CardSubtype.RAT,
                CardSubtype.SPIDER,
                CardSubtype.SQUIRREL
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RegenerateEffect(true)),
                "{T}: Regenerate target Insect, Rat, Spider, or Squirrel.",
                new PermanentPredicateTargetFilter(
                        protectedTypes,
                        "Target must be an Insect, Rat, Spider, or Squirrel"
                )
        ));
    }
}
