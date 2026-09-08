package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "245")
public class BondersEnclave extends Card {

    public BondersEnclave() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {3}, {T}: Draw a card. Activate only if you control a creature with power 4 or greater.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DrawCardEffect(1)),
                "{3}, {T}: Draw a card. Activate only if you control a creature with power 4 or greater."
        ).withRequiredControlledPermanents(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4)
                )),
                1,
                "a creature with power 4 or greater"));
    }
}
