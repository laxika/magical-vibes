package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "128")
public class Boneknitter extends Card {

    public Boneknitter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect(true)),
                "{1}{B}: Regenerate target Zombie.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        "Target must be a Zombie"
                )
        ));
    }
}
