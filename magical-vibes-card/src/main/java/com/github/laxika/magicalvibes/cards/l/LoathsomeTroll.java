package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "192")
public class LoathsomeTroll extends Card {

    public LoathsomeTroll() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new RollD20Effect(
                        returnTo(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY, false),
                        returnTo(GraveyardChoiceDestination.HAND, false),
                        returnTo(GraveyardChoiceDestination.BATTLEFIELD, true))),
                "{3}{G}: Roll a d20. Activate only if this card is in your graveyard."
        ));
    }

    private ReturnCardFromGraveyardEffect returnTo(GraveyardChoiceDestination destination, boolean enterTapped) {
        return ReturnCardFromGraveyardEffect.builder()
                .destination(destination)
                .filter(new CardIsSelfPredicate())
                .returnAll(true)
                .enterTapped(enterTapped)
                .build();
    }
}
