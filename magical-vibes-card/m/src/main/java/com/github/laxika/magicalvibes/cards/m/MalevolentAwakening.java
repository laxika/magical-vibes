package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "147")
public class MalevolentAwakening extends Card {

    public MalevolentAwakening() {
        // {1}{B}{B}, Sacrifice a creature: Return target creature card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{B}",
                List.of(
                        new SacrificeCreatureCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build()
                ),
                "{1}{B}{B}, Sacrifice a creature: Return target creature card from your graveyard to your hand."
        ));
    }
}
