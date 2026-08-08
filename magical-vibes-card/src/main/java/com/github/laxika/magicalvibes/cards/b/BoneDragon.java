package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "88")
public class BoneDragon extends Card {

    public BoneDragon() {
        // {3}{B}{B}, Exile seven other cards from your graveyard: Return this card from your
        // graveyard to the battlefield tapped.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{B}",
                List.of(new ExileNCardsFromGraveyardCost(7, null),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build()),
                "{3}{B}{B}, Exile seven other cards from your graveyard: Return Bone Dragon from your graveyard to the battlefield tapped."));
    }
}
