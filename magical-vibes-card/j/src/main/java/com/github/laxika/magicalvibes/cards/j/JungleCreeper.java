package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "161")
public class JungleCreeper extends Card {

    public JungleCreeper() {
        // {3}{B}{G}: Return Jungle Creeper from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{G}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{3}{B}{G}: Return Jungle Creeper from your graveyard to your hand."
        ));
    }
}
