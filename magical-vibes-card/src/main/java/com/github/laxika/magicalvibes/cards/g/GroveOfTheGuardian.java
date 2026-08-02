package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "240")
public class GroveOfTheGuardian extends Card {

    public GroveOfTheGuardian() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}{W}",
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate(), true),
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                1, "Elemental", 8, 8, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.WHITE),
                                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.VIGILANCE), Set.of())
                ),
                "{3}{G}{W}, {T}, Tap two untapped creatures you control, Sacrifice this land: "
                        + "Create an 8/8 green and white Elemental creature token with vigilance."
        ));
    }
}
