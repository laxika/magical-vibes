package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.KothEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "94")
public class KothOfTheHammer extends Card {

    public KothOfTheHammer() {
        // +1: Untap target Mountain. It becomes a 4/4 red Elemental creature until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new UntapTargetPermanentEffect(),
                        new AnimateLandEffect(4, 4, List.of(CardSubtype.ELEMENTAL), Set.of(), CardColor.RED)
                ),
                "+1: Untap target Mountain. It becomes a 4/4 red Elemental creature until end of turn. It's still a land.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                        "Target must be a Mountain"
                )
        ));

        // −2: Add {R} for each Mountain you control.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new AddManaPerControlledPermanentEffect(ManaColor.RED, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), "Mountains")),
                "\u22122: Add {R} for each Mountain you control."
        ));

        // −5: You get an emblem with "Mountains you control have '{T}: This land deals 1 damage to any target.'"
        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new KothEmblemEffect()),
                "\u22125: You get an emblem with \"Mountains you control have '{T}: This land deals 1 damage to any target.'\"."
        ));
    }
}
