package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLS", collectorNumber = "85")
public class NemataGroveGuardian extends Card {

    public NemataGroveGuardian() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new CreateTokenEffect(1, "Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                "{2}{G}: Create a 1/1 green Saproling creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.SAPROLING)
                                )),
                                "Sacrifice a Saproling"
                        ),
                        new BoostAllCreaturesEffect(1, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.SAPROLING))
                ),
                "Sacrifice a Saproling: Saproling creatures get +1/+1 until end of turn."
        ));
    }
}
