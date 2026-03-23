package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "117")
public class SkirsdagHighPriest extends Card {

    public SkirsdagHighPriest() {
        // Morbid — {T}, Tap two untapped creatures you control: Create a 5/5 black Demon creature token with flying.
        // Activate only if a creature died this turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate(), true),
                        new CreateTokenEffect(
                                "Demon", 5, 5, CardColor.BLACK,
                                List.of(CardSubtype.DEMON),
                                Set.of(Keyword.FLYING), Set.of()
                        )
                ),
                "Morbid — {T}, Tap two untapped creatures you control: Create a 5/5 black Demon creature token with flying. Activate only if a creature died this turn.",
                ActivationTimingRestriction.MORBID
        ));
    }
}
