package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "124")
public class TransmograntAltar extends Card {

    public TransmograntAltar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new SacrificeCreatureCost(), new AwardManaEffect(ManaColor.COLORLESS, 3)),
                "{B}, {T}, Sacrifice a creature: Add {C}{C}{C}."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(),
                        new CreateTokenEffect("Zombie", 3, 3, null,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{2}, {T}, Sacrifice a creature: Create a 3/3 colorless Zombie artifact creature token. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
