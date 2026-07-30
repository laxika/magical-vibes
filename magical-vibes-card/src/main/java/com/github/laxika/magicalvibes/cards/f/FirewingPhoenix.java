package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "131")
public class FirewingPhoenix extends Card {

    public FirewingPhoenix() {
        // {1}{R}{R}{R}: Return Firewing Phoenix from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}{R}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{1}{R}{R}{R}: Return Firewing Phoenix from your graveyard to your hand."
        ));
    }
}
