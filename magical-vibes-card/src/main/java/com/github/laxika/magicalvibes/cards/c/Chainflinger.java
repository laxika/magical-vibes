package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "181")
public class Chainflinger extends Card {

    public Chainflinger() {
        // {1}{R}, {T}: This creature deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{1}{R}, {T}: Chainflinger deals 1 damage to any target."
        ));

        // Threshold — {2}{R}, {T}: This creature deals 2 damage to any target.
        // Activate only if there are seven or more cards in your graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new DealDamageToAnyTargetEffect(2)),
                "{2}{R}, {T}: Chainflinger deals 2 damage to any target. Activate only if there are "
                        + "seven or more cards in your graveyard."
        ).withRequiredGraveyardCards(new CardTruePredicate(), 7, "cards in your graveyard"));
    }
}
