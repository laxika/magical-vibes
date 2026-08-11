package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "90")
public class BloodsoakedAltar extends Card {

    public BloodsoakedAltar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(2),
                        new DiscardCardTypeCost(null, null),
                        new SacrificeCreatureCost(),
                        new CreateTokenEffect("Demon", 5, 5, CardColor.BLACK,
                                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of())
                ),
                "{T}, Pay 2 life, Discard a card, Sacrifice a creature: Create a 5/5 black Demon creature token with flying. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
