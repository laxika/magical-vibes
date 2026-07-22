package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "247")
@CardRegistration(set = "INR", collectorNumber = "200")
public class Groundskeeper extends Card {

    public Groundskeeper() {
        // {1}{G}: Return target basic land card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{G}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(CardPredicateUtils.basicLand())
                        .targetGraveyard(true)
                        .build()),
                "{1}{G}: Return target basic land card from your graveyard to your hand."
        ));
    }
}
