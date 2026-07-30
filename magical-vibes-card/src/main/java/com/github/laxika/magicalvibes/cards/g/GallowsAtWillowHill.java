package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "215")
public class GallowsAtWillowHill extends Card {

    public GallowsAtWillowHill() {
        // {3}, {T}, Tap three untapped Humans you control: Destroy target creature.
        // Its controller creates a 1/1 white Spirit creature token with flying.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(
                        new TapMultiplePermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.HUMAN), true),
                        new DestroyTargetPermanentEffect(false,
                                new CreateTokenEffect(1, "Spirit", 1, 1, CardColor.WHITE,
                                        Set.of(CardColor.WHITE), List.of(CardSubtype.SPIRIT),
                                        Set.of(Keyword.FLYING), Set.of()))
                ),
                "{3}, {T}, Tap three untapped Humans you control: Destroy target creature. "
                        + "Its controller creates a 1/1 white Spirit creature token with flying.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
