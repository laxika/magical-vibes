package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "165")
@CardRegistration(set = "6ED", collectorNumber = "156")
public class StrandsOfNight extends Card {

    public StrandsOfNight() {
        // {B}{B}, Pay 2 life, Sacrifice a Swamp: Return target creature card
        // from your graveyard to the battlefield.
        addActivatedAbility(new ActivatedAbility(
                false, "{B}{B}",
                List.of(
                        new PayLifeCost(2),
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                "Sacrifice a Swamp"),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build()),
                "{B}{B}, Pay 2 life, Sacrifice a Swamp: Return target creature card from your graveyard to the battlefield."
        ));
    }
}
