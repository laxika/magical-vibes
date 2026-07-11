package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "108")
public class SunflareShaman extends Card {

    public SunflareShaman() {
        // {1}{R}, {T}: This creature deals X damage to any target and X damage to itself,
        // where X is the number of Elemental cards in your graveyard.
        CardsInGraveyard elementalsInGraveyard =
                new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.ELEMENTAL), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new DealDamageToAnyTargetEffect(elementalsInGraveyard),
                        new DealDamageToSourceEffect(elementalsInGraveyard)
                ),
                "{1}{R}, {T}: Sunflare Shaman deals X damage to any target and X damage to itself, "
                        + "where X is the number of Elemental cards in your graveyard."
        ));
    }
}
